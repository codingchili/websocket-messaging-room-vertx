package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.*;

/**
 * Created by Robin on 2015-12-17.
 * <p>
 * Handles incoming messages from clients.
 */
enum MessageHandler {
    MESSAGE() {
        @Override
        public void invoke(Parameters params) {
            Message message = (Message) Serializer.unpack(params.data, Message.class);
            message.setSender(params.client.getUsername());
            message.setRoom(params.client.getRoom());

            params.handler.sendRoom(params.client.getRoom(), message);
            params.handler.sendBus(Configuration.NOTIFY(), message);
        }
    },

    JOIN() {
        @Override
        public void invoke(Parameters params) {
            Join join = (Join) Serializer.unpack(params.data, Join.class);

            if (!join.getRoom().equals(params.client.getRoom()))
                params.handler.joinRoom(params.client, new Room().setRoom(join.getRoom()));
            else {
                params.handler.sendCommand(params.client, "Already inside room.");
            }
        }
    },

    TOPIC() {
        @Override
        public void invoke(Parameters params) {
            Topic topic = (Topic) Serializer.unpack(params.data, Topic.class);
            params.handler.trySetTopic(params.client.getRoom(), topic.getTopic(), params.client);
        }
    },

    HELP() {
        @Override
        public void invoke(Parameters params) {
            params.handler.sendCommand(params.client,
                    "[/join <room>, /authenticate <user> <pass>, /connect <host:port>, /topic <string>, /help, /servers]");
        }
    },

    SERVERS() {
        @Override
        public void invoke(Parameters params) {
            params.handler.sendBus(Configuration.NOTIFY(), new ServerList(params.client.getId()));
        }
    };

    public abstract void invoke(Parameters params);
}
