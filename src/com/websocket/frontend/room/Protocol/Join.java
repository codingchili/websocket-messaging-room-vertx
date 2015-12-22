package com.websocket.frontend.room.Protocol;

import com.websocket.frontend.room.Configuration;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Transfer object for a room join-request/reply.
 */
public class Join {
    public static final String ACTION = "join";
    private Header header;
    private String version = Configuration.SERVER_NAME;
    private String room;
    private String topic;

    public Join() {
        header = new Header(ACTION);
    }

    public Join(String room, String topic) {
        this(new Room(room, topic));
    }

    public Join(String room) {
        this(new Room(room, ""));
    }

    public Join(Room room) {
        header = new Header(ACTION);
        this.room = room.getRoom();
        this.topic = room.getTopic();
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
}
