package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.Protocol.*;

/**
 * Created by Robin on 2015-12-17.
 */
public enum EventHandler {
    message() {
        @Override
        public void invoke(Event event) {
            Message message = (Message) Serializer.unpack(event.getData(), Message.class);
            event.getHandler().sendRoom(message.getRoom(), message);
        }
    },

    room() {
        @Override
        public void invoke(Event event) {
            ChatVerticle handler = event.getHandler();
            Room room = (Room) Serializer.unpack(event.getData(), Room.class);

            handler.getRooms().put(room.getRoom(), new ChatRoom(room));
            handler.joinRoom(handler.getClient(room.getHeader().getActor()), room.getRoom());
        }
    },

    authenticate() {
        @Override
        public void invoke(Event event) {
            Authenticate authenticate = (Authenticate) Serializer.unpack(event.getData(), Authenticate.class);
            Message message = new Message("Authentication Failure.");
            ChatVerticle handler = event.getHandler();

            if (authenticate.isCreated())
                message = new Message("Registered account " + authenticate.getUsername());
            else if (authenticate.isAuthenticated())
                message = new Message().setContent("Authenticated.");

            handler.sendBus(authenticate.getHeader().getActor(), message);

            if (authenticate.isAuthenticated()) {
                handler.getClients().get(authenticate.getHeader().getActor()).setAuthenticated(true);
                handler.joinRoom(handler.getClient(authenticate.getHeader().getActor()), ChatVerticle.PUBLIC_ROOM);
            }
        }
    },

    join() {
        @Override
        public void invoke(Event event) {
            UserEvent userEvent = (UserEvent) Serializer.unpack(event.getData(), UserEvent.class);
            ChatVerticle handler = event.getHandler();

            handler.sendRoom(userEvent.getRoom(),
                    new Message(userEvent.getUsername() + " has " +
                            (userEvent.getJoin() ? "joined " : "left") +
                            " the room."));
        }
    },

    history() {
        @Override
        public void invoke(Event event) {
            History history = (History) Serializer.unpack(event.getData(), History.class);
            ChatVerticle handler = event.getHandler();

            for (Message message : history.getList()) {
                handler.sendRoom(history.getRoom(), message.resetHeader());
            }

            handler.notifyRoomLoaded(history.getRoom(),
                    handler.getClient(history.getHeader().getActor()));
        }
    },

    topic() {
        @Override
        public void invoke(Event event) {
            ChatVerticle handler = event.getHandler();
            Topic topic = (Topic) Serializer.unpack(event.getData(), Topic.class);
            handler.setRoomTopic(topic.getRoom(), topic.getTopic(), false);
        }
    },

    servers() {
        @Override
        public void invoke(Event event) {
            ChatVerticle handler = event.getHandler();
            ServerList servers = (ServerList) Serializer.unpack(event.getData(), ServerList.class);

            for (Server server : servers.getList()) {
                handler.sendCommand(handler.getClient(servers.getHeader().getActor())
                        , server.getIp() + ":" + server.getPort() + " - '" + server.getName() + "'.");
            }
        }
    };

    public abstract void invoke(Event event);
}
