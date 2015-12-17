package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.Protocol.*;

/**
 * Created by Robin on 2015-12-17.
 */
// Handles event from a client.
public enum MessageHandler {
    message() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();
            ClientID client = params.getClient();

            Message message = (Message) Serializer.unpack(params.getData(), Message.class);
            message.setSender(client.getUsername());
            message.setRoom(client.getRoom());

            handler.sendRoom(client.getRoom(), message);
            handler.sendBus(NamedBus.NOTIFY(), message);
        }
    },

    authenticate() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();
            Authenticate authenticate = (Authenticate) Serializer.unpack(params.getData(), Authenticate.class);
            authenticate.getHeader().setActor(params.getClient().getId());

            params.getClient().setUsername(authenticate.getUsername());
            handler.sendBus(NamedBus.NOTIFY(), authenticate);
        }
    },

    join() {
        @Override
        public void invoke(Parameters params) {
            Join join = (Join) Serializer.unpack(params.getData(), Join.class);

            if (!join.getRoom().equals(params.getClient().getRoom()))
                params.getHandler().joinRoom(params.getClient(), join.getRoom());
            else
                params.getHandler().sendCommand(params.getClient(), "Already inside room.");
        }
    },

    topic() {
        @Override
        public void invoke(Parameters params) {
            Topic topic = (Topic) Serializer.unpack(params.getData(), Topic.class);
            ChatVerticle handler = params.getHandler();
            ClientID client = params.getClient();
            handler.trySetTopic(client.getRoom(), topic.getTopic(), client);
        }
    },

    help() {
        @Override
        public void invoke(Parameters params) {
            Help help = (Help) Serializer.unpack(params.getData(), Help.class);
            ChatVerticle handler = params.getHandler();
            handler.sendCommand(params.getClient(),
                    "[/join <room>, /authenticate <user> <pass>, /connect <host:port>, /topic <string>, /help, /servers]");
        }
    },

    servers() {
        @Override
        public void invoke(Parameters params) {
            ChatVerticle handler = params.getHandler();
            ClientID client = params.getClient();

            handler.sendBus(NamedBus.NOTIFY(), new ServerList(client.getId()));
        }
    };

    public abstract void invoke(Parameters params);
}
