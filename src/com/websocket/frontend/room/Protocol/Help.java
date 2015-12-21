package com.websocket.frontend.room.Protocol;

/**
 * Created by Robin on 2015-12-16.
 *
 * Command to retrieve the help list.
 */
public class Help {
    public static final String ACTION = "help";
    private Header header = new Header(ACTION);

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
