package com.rduda.frontend.VertChat.Protocol;

/**
 * Created by Robin on 2015-12-16.
 */
public class Room {
    private String topic;
    private String room;
    private String owner;
    private Header header;
    private String username;

    public Room() {
    }

    public Room(String room, String topic) {
        this(room, topic, null, null);
    }

    public Room(String room, String topic, String owner, String id) {
        this.room = room;
        this.topic = topic;
        this.username = owner;
        this.header = new Header("room", id);
    }

    public String getOwner() {
        return owner;
    }

    public Room setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String name) {
        this.room = name;
    }
}
