package com.websocket.frontend.room.Protocol;

import java.util.List;

/**
 * Created by Robin on 2015-12-17.
 *
 * Transfer object for history requests/replies.
 */
public class History {
    public static final String ACTION = "history";
    private String room;
    private List<Message> list;
    private Header header;

    public History() {
    }

    public History(String room, String actor) {
        header = new Header(ACTION, actor);
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<Message> getList() {
        return list;
    }

    public void setList(List<Message> list) {
        this.list = list;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
