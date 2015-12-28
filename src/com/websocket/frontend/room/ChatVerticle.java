package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.*;
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
    private Map<String, AuthenticationHandler> authenticationHandler = new HashMap<>();
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
        authenticationHandler.put(Token.ACTION, AuthenticationHandler.AUTHENTICATE_TOKEN);
        authenticationHandler.put(Authenticate.ACTION, AuthenticationHandler.AUTHENTICATE);

        messageHandler.put(Message.ACTION, MessageHandler.MESSAGE);
        messageHandler.put(Join.ACTION, MessageHandler.JOIN);
        messageHandler.put(Topic.ACTION, MessageHandler.TOPIC);
        messageHandler.put(Help.ACTION, MessageHandler.HELP);
        messageHandler.put(ServerList.ACTION, MessageHandler.SERVERS);

        eventHandler.put(Message.ACTION, EventHandler.MESSAGE);
        eventHandler.put(Authenticate.ACTION, EventHandler.AUTHENTICATE);
        eventHandler.put(Room.ACTION, EventHandler.ROOM);
        eventHandler.put(UserEvent.ACTION, EventHandler.JOIN);
        eventHandler.put(History.ACTION, EventHandler.HISTORY);
        eventHandler.put(Topic.ACTION, EventHandler.TOPIC);
        eventHandler.put(ServerList.ACTION, EventHandler.SERVERS);
    }

    protected Map<String, ChatRoom> getRooms() {
        return rooms;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startServer();
        startEventListener();
        startUserCountLog();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        server.close();
    }

    /**
     * Sets up a WebSocket server that handles incoming messages based on their header.action
     */
    private void startServer() {
        server = vertx.createHttpServer().websocketHandler(event -> {
                    final ClientID client = new ClientID(event.textHandlerID());
                    client.setUsername(event.textHandlerID());

                    event.handler(data -> {
                        Packet packet = (Packet) (Serializer.unpack(data.toString(), Packet.class));
                        System.out.println(data.toString());

                        if (authenticationHandler.get(packet.getAction()) != null)
                            authenticationHandler.get(packet.getAction()).invoke(
                                    new Parameters(data.toString(), event, client, this));

                        else if (client.isAuthenticated())
                            messageHandler.get(packet.getAction()).invoke(
                                    new Parameters(data.toString(), event, client, this));
                        else
                            sendAuthenticationFailed(client);
                    });

                    event.closeHandler(close -> {
                        removeFromRoom(client.getRoom(), client);
                        removeClient(client);
                    });
                    addClient(client);
                    sendRoomData(client, new Join(Configuration.SERVER_ROOM, Configuration.SERVER_TOPIC));
                }
        ).listen(Configuration.LISTEN_PORT);
        System.out.println("Room running on port " + Configuration.LISTEN_PORT);
    }

    private void sendAuthenticationFailed(ClientID client) {
        Authenticate authenticate = new Authenticate().setAuthenticated(false);
        sendBus(client.getId(), authenticate);
    }

    private void startEventListener() {
        vertx.eventBus().consumer(Configuration.EVENT(), handler -> {
            Packet packet = (Packet) Serializer.unpack(handler.body().toString(), Packet.class);
            eventHandler.get(packet.getAction()).invoke(new Event(handler.body().toString(), this));
        });
    }

    private void startUserCountLog() {
        vertx.setPeriodic(Configuration.LOG_INTERVAL, event -> sendBus(Configuration.BUS_LOGGER, new LogUserCount(clients.size())));
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
                // todo  sendMessage(client, "Created room '" + name + "'.");
                notifyRoomEvent(room.getRoom(), client.getUsername(), true);
            } else
                sendBus(Configuration.NOTIFY(), new History(name, client.getId()));

            // todo sendMessage(client, "Room owner \'" + rooms.get(name).getOwner() + "\'.");
            sendRoomData(client, new Join(rooms.get(name).getSettings()));
            addToRoom(name, client);
        } else {
            sendBus(Configuration.NOTIFY(), new RoomEvent(name, RoomEvent.RoomStatus.POPULATED));
            //todo  sendMessage(client, "Loading room..");
            sendBus(Configuration.NOTIFY(), new Room(name, "/topic <string>", client.getUsername(), client.getId()));
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

    protected void notifyRoomEvent(String room, String username, Boolean join) {
        // todo user.event! Message message = new Message(username + " has " + (join ? "joined " : "left") + " the room.");
        // sendRoom(room, message);
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


    private void addClient(ClientID client) {
        clients.put(client.getId(), client);
        loadManager.manage(clients.size());
    }

    private void removeClient(ClientID client) {
        clients.remove(client.getId());
        loadManager.manage(clients.size());
    }

    protected ClientID getClient(String id) {
        return clients.get(id);
    }

    private void sendRoomData(ClientID client, Join join) {
        sendBus(client.getId(), join);
    }


    private void removeFromRoom(String room, ClientID client) {
        if (rooms.get(room) != null) {
            rooms.get(room).remove(client);

            client.setRoom(null);
            notifyRoomEvent(room, client.getUsername(), false);

            if (rooms.get(room).getClients().isEmpty()) {
                rooms.remove(room);

                sendBus(Configuration.NOTIFY(), new RoomEvent(room, RoomEvent.RoomStatus.DEPLETED));
            }
            sendBus(Configuration.NOTIFY(), new UserEvent(room, client.getUsername(), false));
        }
    }

    private void addToRoom(String room, ClientID client) {
        if (client.getRoom() != null)
            removeFromRoom(client.getRoom(), client);

        rooms.get(room).add(client);
        client.setRoom(room);
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
            // todo send TOPIC to clients        .setCommand(true);

            sendRoom(room.getRoom(), message);
            sendBus(Configuration.NOTIFY(), message);
        } else {
            sendCommand(
                    client,
                    "Not authorized, requires owner '" + room.getOwner() + "'.");
            // todo send authenticate:false
        }
    }
}
