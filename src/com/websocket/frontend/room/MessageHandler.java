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

            params.handler.messageRoom(params.client.getRoom(), message);
            params.handler.sendBus(Configuration.UPSTREAM, message);
        }
    },

    JOIN() {
        @Override
        public void invoke(Parameters params) {
            Join join = (Join) Serializer.unpack(params.data, Join.class);

            if (!join.getRoom().equals(params.client.getRoom()))
                params.handler.joinRoom(params.client, new Room().setRoom(join.getRoom()));
            else {
                params.handler.sendBus(params.client.getId(), new Room().setErrorInsideAlready(true));
            }
        }
    },

    TOPIC() {
        @Override
        public void invoke(Parameters params) {
            Topic topic = (Topic) Serializer.unpack(params.data, Topic.class);
            params.handler.trySetTopic(topic.setRoom(params.client.getRoom()), params.client);
        }
    },

    HELP() {
        @Override
        public void invoke(Parameters params) {
            CommandList commands = new CommandList();
            commands.add(new Command("/authenticate <username> <passwd>", ""));
            commands.add(new Command("/join <room>", ""));
            commands.add(new Command("/topic <topic>", ""));
            commands.add(new Command("/help", ""));
            commands.add(new Command("/servers", ""));

            params.handler.sendBus(params.client.getId(), commands);
        }
    },

    SERVERS() {
        @Override
        public void invoke(Parameters params) {
            params.handler.sendBus(Configuration.UPSTREAM, new ServerList(params.client.getId()));
        }
    };

    public abstract void invoke(Parameters params);
}
