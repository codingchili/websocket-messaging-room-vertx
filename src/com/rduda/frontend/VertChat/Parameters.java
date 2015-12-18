package com.rduda.frontend.VertChat;

import io.vertx.core.http.ServerWebSocket;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Passed to a Message Handler for handling messages from an user.
 */
class Parameters {
    private String data;
    private ServerWebSocket socket;
    private ClientID client;
    private ChatVerticle handler;

    public Parameters(String data, ServerWebSocket socket, ClientID client, ChatVerticle handler) {
        this.data = data;
        this.socket = socket;
        this.client = client;
        this.handler = handler;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ServerWebSocket getSocket() {
        return socket;
    }

    public void setSocket(ServerWebSocket socket) {
        this.socket = socket;
    }

    public ClientID getClient() {
        return client;
    }

    public void setClient(ClientID client) {
        this.client = client;
    }

    public ChatVerticle getHandler() {
        return handler;
    }

    public void setHandler(ChatVerticle handler) {
        this.handler = handler;
    }
}
