package com.rduda.frontend.VertChat.Protocol;

import com.rduda.frontend.VertChat.Configuration;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Transfer object for querying a room or returning a query response.
 */
public class Room {
    private String topic = Configuration.SERVER_TOPIC;
    private String room = Configuration.DEFAULT_ROOM;
    private String owner;
    private Header header;
    private String username;
    private Boolean created = false;

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

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
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

    public Room setRoom(String name) {
        this.room = name;
        return this;
    }
}
