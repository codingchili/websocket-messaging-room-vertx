package com.websocket.frontend.room;

/**
 * Created by Robin on 2015-12-16.
 *
 * Used to pass data to an Event Handler.
 */
class Event {
    private String data;
    private ChatVerticle handler;

    public Event(String data, ChatVerticle handler) {
        this.data = data;
        this.handler = handler;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ChatVerticle getHandler() {
        return handler;
    }

    public String getData() {
        return data;
    }
}
