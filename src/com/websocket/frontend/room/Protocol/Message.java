package com.websocket.frontend.room.Protocol;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Message transfer object.
 */
public class Message {
    public static final String ACTION = "message";
    private String sender;
    private String content;
    private String room;
    private Header header;
    private Boolean command;

    public Message() {
        this("");
    }

    public Message(String content) {
        this(content, null);
    }

    public Message(String content, String room) {
        this.content = content;
        this.room = room;
        this.header = new Header(ACTION);
    }

    public Message resetHeader() {
        this.header = new Header(ACTION);
        return this;
    }

    public String getContent() {
        return content;
    }

    public Message setContent(String content) {
        this.content = content;
        return this;
    }

    public String getSender() {
        return sender;
    }

    public Message setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getRoom() {
        return room;
    }

    public Message setRoom(String room) {
        this.room = room;
        return this;
    }

    public Boolean getCommand() {
        return command;
    }

    public Message setCommand(Boolean command) {
        this.command = command;
        return this;
    }
}