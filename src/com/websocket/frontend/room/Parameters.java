package com.websocket.frontend.room;

import io.vertx.core.http.ServerWebSocket;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Passed to a Message Handler for handling messages from an user.
 */
class Parameters {
    public String data;
    public ServerWebSocket socket;
    public ClientID client;
    public ChatVerticle handler;

    public Parameters(String data, ServerWebSocket socket, ClientID client, ChatVerticle handler) {
        this.data = data;
        this.socket = socket;
        this.client = client;
        this.handler = handler;
    }

    public String getAddress() {
        return socket.textHandlerID();
    }
}
