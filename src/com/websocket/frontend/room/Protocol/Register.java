package com.websocket.frontend.room.Protocol;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Message to Register the chat-server with the backend.
 */
public class Register {
    public static final String ACTION = "register";
    private String name;
    private Header header;
    private String port;

    public Register() {
    }

    public Register(String name, String port) {
        this.name = name;
        this.port = port;
        this.header = new Header(ACTION);
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}

