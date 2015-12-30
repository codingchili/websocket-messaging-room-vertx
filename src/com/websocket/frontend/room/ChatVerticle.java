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
                    sendBus(client.getId(), new Room(Configuration.SERVER_ROOM, Configuration.SERVER_TOPIC).setSystem(true));
                }
        ).listen(Configuration.LISTEN_PORT);
        System.out.println("Room running on port " + Configuration.LISTEN_PORT);
    }

    private void sendAuthenticationFailed(ClientID client) {
        Authenticate authenticate = new Authenticate().setAuthenticated(false);
        sendBus(client.getId(), authenticate);
    }

    private void startEventListener() {
        vertx.eventBus().consumer(Configuration.DOWNSTREAM, handler -> {
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
            sendBus(client.getId(), new Room(rooms.get(name).getSettings(), room.getCreated()));
            messageRoom(room.getRoom(), new UserEvent(room.getRoom(), client.getUsername(), true));
            sendBus(Configuration.UPSTREAM, new UserEvent(room.getRoom(), client.getUsername(), true));
            addToRoom(name, client);
        } else {
            sendBus(Configuration.UPSTREAM, new RoomEvent(name, RoomEvent.RoomStatus.POPULATED));
            sendBus(Configuration.UPSTREAM, new Room(name, "/topic <string>", client.getUsername(), client.getId()));
        }
    }

    private void addToRoom(String name, ClientID client) {
        ChatRoom room = rooms.get(name);

        if (client.getRoom() != null)
            removeFromRoom(client.getRoom(), client);

        if (room != null) {
            room.add(client);
            client.setRoom(name);
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


    private void removeFromRoom(String room, ClientID client) {
        if (rooms.get(room) != null) {
            rooms.get(room).remove(client);

            client.setRoom(null);
            messageRoom(room, new UserEvent(room, client.getUsername(), false));

            if (rooms.get(room).getClients().isEmpty()) {
                rooms.remove(room);

                sendBus(Configuration.UPSTREAM, new RoomEvent(room, RoomEvent.RoomStatus.DEPLETED));
            }
            sendBus(Configuration.UPSTREAM, new UserEvent(room, client.getUsername(), false));
        }
    }

    /**
     * Broadcast a message to all users within the same room.
     *
     * @param name    of the room in which the message should be sent.
     * @param message which should be sent.
     */
    protected void messageRoom(String name, Object message) {
        ChatRoom room = rooms.get(name);

        if (room != null) {
            if (message instanceof Message)
                room.addHistory((Message) message);

            for (ClientID client : room.getClients().values()) {
                sendBus(client.getId(), message);
            }
        }
    }


    protected Map<String, ClientID> getClients() {
        return clients;
    }

    /**
     * Request a change of the rooms topic, the request is rejected when the requestor is not the owner
     * of the room.
     *
     * @param topic  the new topic.
     * @param client the initiator of the request.
     */
    protected void trySetTopic(Topic topic, ClientID client) {
        Room room = rooms.get(topic.getRoom()).getSettings();

        if (room.getOwner().equals(client.getUsername())) {
            setRoomTopic(topic, true);
        } else {
            messageRoom(topic.getRoom(), new Topic().setRejected(true));
        }
    }


    /**
     * Sets the topic for a room.
     *
     * @param topic            the topic and room.
     * @param locallyInitiated indicates whether a directly connected client initiated the change.
     *                         if set to false indicating that it was received as an event. (should not be broadcast)
     */
    protected void setRoomTopic(Topic topic, boolean locallyInitiated) {
        ChatRoom room = rooms.get(topic.getRoom());
        room.getSettings().setTopic(topic.getTopic());

        for (ClientID client : room.getClients().values())
            sendBus(client.getId(), topic);

        if (locallyInitiated)
            sendBus(Configuration.UPSTREAM, topic);
    }
}
