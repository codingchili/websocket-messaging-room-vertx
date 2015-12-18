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
    public final static String DEFAULT_ROOM = "SYSTEM";
    public final static String PUBLIC_ROOM = "General";
    private final static String SERVER_NAME = "VERT.X";
    private final static String DEFAULT_TOPIC = "Authentication Required";
    private Map<String, MessageHandler> messageHandler = new HashMap<>();
    private Map<String, EventHandler> eventHandler = new HashMap<>();
    private Map<String, ClientID> clients = new HashMap<>();
    private Map<String, ChatRoom> rooms = new HashMap<>();
    private Vertx vertx;
    private HttpServer server;


    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        // Bind action names to methods, using an enum.
        messageHandler.put("message", MessageHandler.message);
        messageHandler.put("authenticate", MessageHandler.authenticate);
        messageHandler.put("join", MessageHandler.join);
        messageHandler.put("topic", MessageHandler.topic);
        messageHandler.put("help", MessageHandler.help);
        messageHandler.put("server.list", MessageHandler.servers);

        eventHandler.put("message", EventHandler.message);
        eventHandler.put("authenticate", EventHandler.authenticate);
        eventHandler.put("room", EventHandler.room);
        eventHandler.put("user.event", EventHandler.join);
        eventHandler.put("history", EventHandler.history);
        eventHandler.put("topic", EventHandler.topic);
        eventHandler.put("server.list", EventHandler.servers);
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
        vertx.eventBus().consumer(NamedBus.EVENT(), handler -> {
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
     * @param name   the name of the room to join.
     */
    protected void joinRoom(ClientID client, String name) {
        if (rooms.containsKey(name)) {
            sendMessage(client, "Room owner \'" + rooms.get(name).getOwner() + "\'.");
            sendRoomData(client, new Join(rooms.get(name).getSettings()));
            addToRoom(name, client);

            sendBus(NamedBus.NOTIFY(), new History(name, client.getId()));
        } else {
            sendMessage(client, "Loading room..");
            sendBus(NamedBus.NOTIFY(), new Room(name, "/topic <string>", client.getUsername(), client.getId()));
        }
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
        Message message = new Message()
                .setContent(client.getUsername() + " has joined the room.");

        notifyRoomEvent(room, message);
        sendBus(NamedBus.NOTIFY(), new UserEvent(room, client.getUsername(), true));
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

                // All packets except authenticate requires authentication.
                if (!packet.getHeader().getAction().equals("authenticate") && !client.isAuthenticated())
                    notAuthenticated(client);
                else
                    messageHandler.get(packet.getHeader().getAction()).invoke(
                            new Parameters(event.toString(), socket, client, this)
                    );
            });

            // Whenever a client disconnects for any reason, it must be removed from the joined room.
            socket.closeHandler(event -> {
                removeFromRoom(client.getRoom(), client);
                removeClient(client);
            });

            onClientConnect(client);
        }).listen(ChatServer.LISTEN_PORT, "localhost");
    }

    private void onClientConnect(ClientID client) {
        addClient(client);
        sendRoomData(client, new Join(DEFAULT_ROOM, DEFAULT_TOPIC));
        sendMessage(client, "Connected to " + SERVER_NAME);
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

            notifyRoomEvent(room, new Message()
                    .setContent(client.getUsername() + " has left the room."));

            // unload the room if empty, triggers a reload on next join.
            if (rooms.get(room).getClients().isEmpty())
                rooms.remove(room);

            sendBus(NamedBus.NOTIFY(), new UserEvent(room, client.getUsername(), false));
        }
    }

    private void addToRoom(String room, ClientID client) {
        if (client.getRoom() != null)
            removeFromRoom(client.getRoom(), client);

        rooms.get(room).add(client);
        client.setRoom(room);
    }

    private void notifyRoomEvent(String name, Message message) {
        HashMap<String, ClientID> clients = rooms.get(name).getClients();

        for (ClientID client : clients.values()) {
            sendBus(client.getId(), message);
        }
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
            sendBus(NamedBus.NOTIFY(), new Topic(name, topic));
    }

    /**
     * Broadcast a message to all users within the same room.
     *
     * @param name    of the room in which the message should be sent.
     * @param message which should be sent.
     */
    protected void sendRoom(String name, Message message) {
        ChatRoom room = rooms.get(name);

        if (room != null && room.getClients().size() != 0)
            for (ClientID client : rooms.get(name).getClients().values()) {
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
        Message message = new Message(content).setCommand(true);
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
            sendBus(NamedBus.NOTIFY(), message);
        } else {
            sendCommand(
                    client,
                    "Not authorized, requires owner '" + room.getOwner() + "'.");
        }
    }
}
