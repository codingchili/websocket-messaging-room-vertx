package com.websocket.frontend.room.Protocol;

import com.websocket.frontend.room.Configuration;

import java.util.ArrayList;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Transfer object for a room join-request/reply.
 */
public class Join {
    public static final String ACTION = "join";
    private Header header;
    private String room;
    private String topic;
    private Boolean created;
    private ArrayList<Message> history = new ArrayList<>();

    public Join() {
        header = new Header(ACTION);
    }

    public Join(String room, String topic) {
        this(new Room(room, topic), false);
    }

    public Join(String room) {
        this(new Room(room, ""), false);
    }

    public Join(Room room, Boolean created) {
        header = new Header(ACTION);
        this.room = room.getRoom();
        this.topic = room.getTopic();
        this.history = room.getHistory();
        this.created = created;
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

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public ArrayList<Message> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Message> history) {
        this.history = history;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }
}
