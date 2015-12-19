package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.Protocol.*;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Handles messages from the client and passes them to the EventHandler.
 */
class ChatVerticle implements Verticle {
    private Map<String, MessageHandler> messageHandler = new HashMap<>();
    private Map<String, EventHandler> eventHandler = new HashMap<>();
    private Map<String, ClientID> clients = new HashMap<>();
    private Map<String, ChatRoom> rooms = new HashMap<>();
    private LoadManager loadManager;
    private Vertx vertx;
    private HttpServer server;


    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        loadManager = new LoadManager(vertx);

        // Bind action names to methods, using an enum.
        messageHandler.put("message", MessageHandler.MESSAGE);
        messageHandler.put("authenticate", MessageHandler.AUTHENTICATE);
        messageHandler.put("join", MessageHandler.JOIN);
        messageHandler.put("topic", MessageHandler.TOPIC);
        messageHandler.put("help", MessageHandler.HELP);
        messageHandler.put("server.list", MessageHandler.SERVERS);

        eventHandler.put("message", EventHandler.MESSAGE);
        eventHandler.put("authenticate", EventHandler.AUTHENTICATE);
        eventHandler.put("room", EventHandler.ROOM);
        eventHandler.put("user.event", EventHandler.JOIN);
        eventHandler.put("history", EventHandler.HISTORY);
        eventHandler.put("topic", EventHandler.TOPIC);
        eventHandler.put("server.list", EventHandler.SERVERS);
    }

    protected Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startServer();
        startEventListener();
    }

    private void startEventListener() {
        vertx.eventBus().consumer(Configuration.EVENT(), handler -> {
            Packet packet = (Packet) Serializer.unpack(handler.body().toString(), Packet.class);
            eventHandler.get(packet.getHeader().getAction()).invoke(new Event(handler.body().toString(), this));
        });
    }

    /**
     * Triggers the event chain for joining a room,
     * the room is cached locally: the client is moved to the room and the room is notified.
     * the room does not exist in this context: the server is queried for the room
     * and returns the existing, or creates a new. This method is used as a callback for completion.
     *
     * @param client the client joining the room.
     * @param room   the room to be joined.
     */
    protected void joinRoom(ClientID client, Room room) {
        String name = room.getRoom();

        if (rooms.containsKey(name)) {

            if (room.getCreated()) {
                sendMessage(client, "Created room '" + name + "'.");
                notifyRoomEvent(room.getRoom(), client.getUsername(), true);
            } else
                sendBus(Configuration.NOTIFY(), new History(name, client.getId()));

            sendMessage(client, "Room owner \'" + rooms.get(name).getOwner() + "\'.");
            sendRoomData(client, new Join(rooms.get(name).getSettings()));
            addToRoom(name, client);
        } else {
            sendBus(Configuration.NOTIFY(), new RoomEvent(name, RoomEvent.RoomStatus.POPULATED));
            sendMessage(client, "Loading room..");
            sendBus(Configuration.NOTIFY(), new Room(name, "/topic <string>", client.getUsername(), client.getId()));
        }
    }


    protected void notifyRoomEvent(String room, String username, Boolean join) {
        Message message = new Message(username + " has " + (join ? "joined " : "left") + " the room.");
        sendRoom(room, message);
    }

    /**
     * Pushes a message onto the event bus for the given address.
     *
     * @param address of the bus.
     * @param data    as an object that is Serializable to JSON.
     */
    protected void sendBus(String address, Object data) {
        vertx.eventBus().send(address, Serializer.pack(data));
    }

    /**
     * Notify the clients within a room that a new client has joined the room.
     *
     * @param room   the name of the room that was joined.
     * @param client joinee.
     */
    protected void notifyRoomLoaded(String room, ClientID client) {
        notifyRoomEvent(room, client.getUsername(), true);
        sendBus(Configuration.NOTIFY(), new UserEvent(room, client.getUsername(), true));
    }

    /**
     * Sets up a WebSocket server that handles incoming messages based on their header.action
     */
    private void startServer() {
        server = vertx.createHttpServer().websocketHandler(socket -> {
            final ClientID client = new ClientID(socket.textHandlerID());
            client.setUsername(socket.textHandlerID());

            socket.handler(event -> {
                Packet packet = (Packet) (Serializer.unpack(event.toString(), Packet.class));

                if (messageHandler.get(packet.getHeader().getAction()) != null) {
                    if (!packet.getHeader().getAction().equals("authenticate") && !client.isAuthenticated())
                        notAuthenticated(client);
                    else
                        messageHandler.get(packet.getHeader().getAction()).invoke(
                                new Parameters(event.toString(), socket, client, this)
                        );
                }
            });

            // Whenever a client disconnects for any reason, it must be removed from the joined room.
            socket.closeHandler(event -> {
                removeFromRoom(client.getRoom(), client);
                removeClient(client);
            });

            onClientConnect(client);
        }).listen(Configuration.LISTEN_PORT);
    }

    private void onClientConnect(ClientID client) {
        addClient(client);
        sendRoomData(client, new Join(Configuration.SERVER_ROOM, Configuration.SERVER_TOPIC));
        sendMessage(client, "Connected to " + Configuration.SERVER_NAME);
        sendAuthenticationRequired(client);
    }

    private void sendMessage(ClientID client, String content) {
        Message message = new Message(content);
        sendBus(client.getId(), message);
    }

    private void sendAuthenticationRequired(ClientID client) {
        sendMessage(client, "Authentication Required.");
        sendMessage(client, "/authenticate <user> <password>");
    }

    private void addClient(ClientID client) {
        clients.put(client.getId(), client);
    }

    private void removeClient(ClientID client) {
        clients.remove(client.getId());
    }

    protected ClientID getClient(String id) {
        return clients.get(id);
    }

    private void sendRoomData(ClientID client, Join join) {
        sendBus(client.getId(), join);
    }

    private void notAuthenticated(ClientID client) {
        sendBus(client.getId(), new Message()
                .setContent("Authentication Required."));

        sendBus(client.getId(), new Message()
                .setContent("/authenticate <username> <password>"));
    }

    private void removeFromRoom(String room, ClientID client) {
        if (rooms.get(room) != null) {
            rooms.get(room).remove(client);

            client.setRoom(null);
            notifyRoomEvent(room, client.getUsername(), false);

            // unload the room if empty, triggers a reload on next join.
            if (rooms.get(room).getClients().isEmpty()) {
                rooms.remove(room);

                sendBus(Configuration.NOTIFY(), new RoomEvent(room, RoomEvent.RoomStatus.DEPLETED));
            }
            sendBus(Configuration.NOTIFY(), new UserEvent(room, client.getUsername(), false));
        }
        loadManager.manage(clients.size());
    }

    private void addToRoom(String room, ClientID client) {
        if (client.getRoom() != null)
            removeFromRoom(client.getRoom(), client);

        rooms.get(room).add(client);
        client.setRoom(room);
        loadManager.manage(clients.size());
    }

    /**
     * Sets the topic for a room.
     *
     * @param name             the room which should have its topic changed.
     * @param topic            the topic itself.
     * @param locallyInitiated indicates whether a directly connected client initiated the change.
     *                         if set to false indicating that it was received as an event. (should not be broadcast)
     */
    protected void setRoomTopic(String name, String topic, boolean locallyInitiated) {
        ChatRoom room = rooms.get(name);
        Join data = new Join(name, topic);
        room.getSettings().setTopic(topic);

        for (ClientID client : room.getClients().values())
            sendRoomData(client, data);

        if (locallyInitiated)
            sendBus(Configuration.NOTIFY(), new Topic(name, topic));
    }

    /**
     * Broadcast a message to all users within the same room.
     *
     * @param name    of the room in which the message should be sent.
     * @param message which should be sent.
     */
    protected void sendRoom(String name, Message message) {
        ChatRoom room = rooms.get(name);

        if (room != null)
            for (ClientID client : room.getClients().values()) {
                sendBus(client.getId(), message);
            }
    }

    /**
     * Sends a message tagged as a command.
     *
     * @param client  the receiver of the message.
     * @param content the content of the message.
     */
    protected void sendCommand(ClientID client, String content) {
        Message message = new Message(content).setCommand(false);
        sendBus(client.getId(), message);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        server.close();
    }

    protected Map<String, ClientID> getClients() {
        return clients;
    }

    /**
     * Request a change of the rooms topic, the request is rejected when the requestor is not the owner
     * of the room.
     *
     * @param roomName name of the room which should have the topic changed.
     * @param topic    the new topic.
     * @param client   the initiator of the request.
     */
    protected void trySetTopic(String roomName, String topic, ClientID client) {
        Room room = rooms.get(roomName).getSettings();

        if (room.getOwner().equals(client.getUsername())) {
            setRoomTopic(client.getRoom(), topic, true);

            Message message = new Message()
                    .setContent(client.getUsername() + " changed the topic to " + topic)
                    .setRoom(roomName)
                    .setCommand(true);

            sendRoom(room.getRoom(), message);
            sendBus(Configuration.NOTIFY(), message);
        } else {
            sendCommand(
                    client,
                    "Not authorized, requires owner '" + room.getOwner() + "'.");
        }
    }
}
