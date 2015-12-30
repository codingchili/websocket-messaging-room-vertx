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
            event.handler.messageRoom(message.getRoom(), message);
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

            ClientID client = event.handler.getClients().get(event.actor);
            client.setUsername(authenticate.getUsername());
            client.setAuthenticated(authenticate.isAuthenticated());
            event.handler.sendBus(event.actor, authenticate);
        }
    },

    JOIN() {
        @Override
        public void invoke(Event event) {
            UserEvent userEvent = (UserEvent) Serializer.unpack(event.data, UserEvent.class);
            event.handler.messageRoom(userEvent.getRoom(), userEvent);
        }
    },

    TOPIC() {
        @Override
        public void invoke(Event event) {
            Topic topic = (Topic) Serializer.unpack(event.data, Topic.class);
            event.handler.setRoomTopic(topic, false);
        }
    },

    SERVERS() {
        @Override
        public void invoke(Event event) {
            ServerList servers = (ServerList) Serializer.unpack(event.data, ServerList.class);
            event.handler.sendBus(event.handler.getClient(event.actor).getId(), servers);
        }
    };

    public abstract void invoke(Event event);
}
