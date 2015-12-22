package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.*;

/**
 * Created by Robin on 2015-12-17.
 * <p>
 * Handles events received from the connector backend service.
 */
enum EventHandler {
    MESSAGE() {
        @Override
        public void invoke(Event event) {
            Message message = (Message) Serializer.unpack(event.data, Message.class);
            event.handler.sendRoom(message.getRoom(), message);
        }
    },

    ROOM() {
        @Override
        public void invoke(Event event) {
            Room room = (Room) Serializer.unpack(event.data, Room.class);
            event.handler.getRooms().put(room.getRoom(), new ChatRoom(room));
            event.handler.joinRoom(event.handler.getClient(event.actor), room);
        }
    },

    AUTHENTICATE() {
        @Override
        public void invoke(Event event) {
            Authenticate authenticate = (Authenticate) Serializer.unpack(event.data, Authenticate.class);

            authenticate.getHeader().setActor(null);

            event.handler.sendBus(event.actor, authenticate);
            event.handler.getClients().get(event.actor).setAuthenticated(authenticate.isAuthenticated());
        }
    },

    JOIN() {
        @Override
        public void invoke(Event event) {
            UserEvent userEvent = (UserEvent) Serializer.unpack(event.data, UserEvent.class);

            event.handler.notifyRoomEvent(userEvent.getRoom(), userEvent.getUsername(), userEvent.getJoin());
        }
    },

    HISTORY() {
        @Override
        public void invoke(Event event) {
            History history = (History) Serializer.unpack(event.data, History.class);

            for (Message message : history.getList()) {
                event.handler.sendBus(event.actor, message.resetHeader());
            }

            event.handler.notifyRoomLoaded(history.getRoom(),
                    event.handler.getClient(event.actor));
        }
    },

    TOPIC() {
        @Override
        public void invoke(Event event) {
            Topic topic = (Topic) Serializer.unpack(event.data, Topic.class);
            event.handler.setRoomTopic(topic.getRoom(), topic.getTopic(), false);
        }
    },

    SERVERS() {
        @Override
        public void invoke(Event event) {
            ServerList servers = (ServerList) Serializer.unpack(event.data, ServerList.class);

            for (Server server : servers.getList()) {
                event.handler.sendCommand(event.handler.getClient(event.actor),
                        server.getIp() + ":" + server.getPort() + " - '" + server.getName()
                                + "', State = " + (server.getFull() ? "FULL" : "AVAILABLE") + ".");
            }
        }
    };

    public abstract void invoke(Event event);
}
