package com.websocket.frontend.room.Protocol;

import java.util.List;

/**
 * Created by Robin on 2015-12-16.
 *
 * Transfer object for requesting/returning a list of the currently connected servers.
 */
public class ServerList {
    public static final String ACTION = "server.list";
    private Header header;
    private List<Server> list;

    public ServerList() {
        this("");
    }

    public ServerList(String actor) {
        this.header = new Header(ACTION, actor);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Server> getList() {
        return list;
    }

    public void setList(List<Server> list) {
        this.list = list;
    }
}
