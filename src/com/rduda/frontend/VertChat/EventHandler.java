package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.Protocol.*;

/**
 * Created by Robin on 2015-12-17.
 * <p>
 * Handles events received from the connector backend service.
 */
enum EventHandler {
    MESSAGE() {
        @Override
        public void invoke(Event event) {
            Message message = (Message) Serializer.unpack(event.getData(), Message.class);
            event.getHandler().sendRoom(message.getRoom(), message);
        }
    },

    ROOM() {
        @Override
        public void invoke(Event event) {
            ChatVerticle handler = event.getHandler();
            Room room = (Room) Serializer.unpack(event.getData(), Room.class);

            handler.getRooms().put(room.getRoom(), new ChatRoom(room));
            handler.joinRoom(handler.getClient(room.getHeader().getActor()), room);
        }
    },

    AUTHENTICATE() {
        @Override
        public void invoke(Event event) {
            Authenticate authenticate = (Authenticate) Serializer.unpack(event.getData(), Authenticate.class);
            ChatVerticle handler = event.getHandler();

            handler.sendBus(authenticate.getHeader().getActor(), authenticate);
            handler.getClients().get(authenticate.getHeader().getActor()).setAuthenticated(authenticate.isAuthenticated());
        }
    },

    JOIN() {
        @Override
        public void invoke(Event event) {
            UserEvent userEvent = (UserEvent) Serializer.unpack(event.getData(), UserEvent.class);
            ChatVerticle handler = event.getHandler();

            handler.notifyRoomEvent(userEvent.getRoom(), userEvent.getUsername(), userEvent.getJoin());
        }
    },

    HISTORY() {
        @Override
        public void invoke(Event event) {
            History history = (History) Serializer.unpack(event.getData(), History.class);
            ChatVerticle handler = event.getHandler();

            for (Message message : history.getList()) {
                handler.sendBus(history.getHeader().getActor(), message.resetHeader());
            }

            handler.notifyRoomLoaded(history.getRoom(),
                    handler.getClient(history.getHeader().getActor()));
        }
    },

    TOPIC() {
        @Override
        public void invoke(Event event) {
            ChatVerticle handler = event.getHandler();
            Topic topic = (Topic) Serializer.unpack(event.getData(), Topic.class);
            handler.setRoomTopic(topic.getRoom(), topic.getTopic(), false);
        }
    },

    SERVERS() {
        @Override
        public void invoke(Event event) {
            ChatVerticle handler = event.getHandler();
            ServerList servers = (ServerList) Serializer.unpack(event.getData(), ServerList.class);

            for (Server server : servers.getList()) {
                handler.sendCommand(handler.getClient(servers.getHeader().getActor()),
                        server.getIp() + ":" + server.getPort() + " - '" + server.getName()
                                + "', State = " + (server.getFull() ? "FULL" : "AVAILABLE") + ".");
            }
        }
    };

    public abstract void invoke(Event event);
}
