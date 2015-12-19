package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.Protocol.*;

/**
 * Created by Robin on 2015-12-17.
 * <p>
 * Handles incoming messages from clients.
 */
enum MessageHandler {
    MESSAGE() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();
            ClientID client = params.getClient();

            Message message = (Message) Serializer.unpack(params.getData(), Message.class);
            message.setSender(client.getUsername());
            message.setRoom(client.getRoom());

            handler.sendRoom(client.getRoom(), message);
            handler.sendBus(Configuration.NOTIFY(), message);
        }
    },

    AUTHENTICATE() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();
            Authenticate authenticate = (Authenticate) Serializer.unpack(params.getData(), Authenticate.class);

            if (params.getClient().isAuthenticated()) {
                params.getHandler().sendCommand(params.getClient(), "Already authenticated, use /logout.");
            } else {
                authenticate.getHeader().setActor(params.getClient().getId());
                params.getClient().setUsername(authenticate.getUsername());
                handler.sendBus(Configuration.NOTIFY(), authenticate);
            }
        }
    },

    JOIN() {
        @Override
        public void invoke(Parameters params) {
            Join join = (Join) Serializer.unpack(params.getData(), Join.class);

            if (!join.getRoom().equals(params.getClient().getRoom()))
                params.getHandler().joinRoom(params.getClient(), new Room().setRoom(join.getRoom()));
            else {
                params.getHandler().sendCommand(params.getClient(), "Already inside room.");
            }
        }
    },

    TOPIC() {
        @Override
        public void invoke(Parameters params) {
            Topic topic = (Topic) Serializer.unpack(params.getData(), Topic.class);
            ChatVerticle handler = params.getHandler();
            ClientID client = params.getClient();
            handler.trySetTopic(client.getRoom(), topic.getTopic(), client);
        }
    },

    HELP() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();

            handler.sendCommand(params.getClient(),
                    "[/join <room>, /authenticate <user> <pass>, /connect <host:port>, /topic <string>, /help, /servers]");
        }
    },

    SERVERS() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();
            ClientID client = params.getClient();

            handler.sendBus(Configuration.NOTIFY(), new ServerList(client.getId()));
        }
    };

    public abstract void invoke(Parameters params);
}
