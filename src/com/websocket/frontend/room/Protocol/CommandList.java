package com.websocket.frontend.room.Protocol;

import java.util.ArrayList;

/**
 * Created by Robin on 2015-12-22.
 * <p>
 * A list of commands.
 */
public class CommandList {
    public final static String ACTION = "help";
    private ArrayList<Command> list = new ArrayList<>();
    private Header header;

    public CommandList() {
        this.header = new Header(ACTION);
    }

    public ArrayList<Command> getList() {
        return list;
    }

    public void setList(ArrayList<Command> list) {
        this.list = list;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public void add(Command command) {
        list.add(command);
    }
}
