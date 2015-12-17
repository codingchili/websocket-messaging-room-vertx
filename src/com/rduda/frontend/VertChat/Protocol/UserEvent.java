package com.rduda.frontend.VertChat.Protocol;

/**
 * Created by Robin on 2015-12-17.
 */
public class UserEvent {
    private Boolean join;
    private String room;
    private String username;
    private Header header;

    public UserEvent() {
    }

    public UserEvent(String room, String username, Boolean join) {
        this.room = room;
        this.username = username;
        this.join = join;
        this.header = new Header("user.event");
    }

    public Boolean getJoin() {
        return join;
    }

    public void setJoin(Boolean join) {
        this.join = join;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
