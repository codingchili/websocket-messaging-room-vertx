package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.Packet;
import com.websocket.frontend.room.Protocol.Serializer;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Used to pass data to an Event Handler.
 */
class Event {
    public String data;
    public ChatVerticle handler;
    public Packet packet;
    public String actor;

    public Event(String data, ChatVerticle handler) {
        this.data = data;
        this.handler = handler;
        this.packet = (Packet) Serializer.unpack(data, Packet.class);
        this.actor = packet.getHeader().getActor();
    }
}
