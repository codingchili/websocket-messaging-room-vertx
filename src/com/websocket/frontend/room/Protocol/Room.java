package com.websocket.frontend.room.Protocol;

import com.websocket.frontend.room.Configuration;

import java.util.ArrayList;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Transfer object for querying a room or returning a query response.
 */
public class Room {
    public static final String ACTION = "room";
    private String topic = Configuration.SERVER_TOPIC;
    private String room = Configuration.DEFAULT_ROOM;
    private String version = Configuration.SERVER_NAME;
    private String owner;
    private Header header;
    private String username;
    private Boolean created = false;
    private Boolean errorInsideAlready;
    private Boolean system = false;
    private ArrayList<Message> history = new ArrayList<>();

    public Room() {
        this.header = new Header(ACTION);
    }


    public Room(String room, String topic) {
        this(room, topic, null, null);
    }

    public Room(String room, String topic, String owner, String id) {
        this.room = room;
        this.topic = topic;
        this.username = owner;
        this.header = new Header(ACTION, id);
    }

    public Room(Room room, Boolean created) {
        this(room.getRoom(), room.getTopic(), room.getOwner(), null);
        this.created = created;
    }

    public String getOwner() {
        return owner;
    }

    public Room setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Boolean getErrorInsideAlready() {
        return errorInsideAlready;
    }

    public Room setErrorInsideAlready(Boolean errorInsideAlready) {
        this.errorInsideAlready = errorInsideAlready;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getSystem() {
        return system;
    }

    public Room setSystem(Boolean system) {
        this.system = system;
        return this;
    }

    public ArrayList<Message> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Message> history) {
        this.history = history;
    }
}
