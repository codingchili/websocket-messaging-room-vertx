package com.rduda.frontend.VertChat.Protocol;

import java.util.List;

/**
 * Created by Robin on 2015-12-16.
 */
public class ServerList {
    private Header header;
    private List<Server> list;

    public ServerList() {
    }

    public ServerList(String actor) {
        this.header = new Header("server.list", actor);
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
