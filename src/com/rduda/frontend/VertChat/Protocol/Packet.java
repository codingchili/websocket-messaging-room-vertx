package com.rduda.frontend.VertChat.Protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Robin on 2015-12-16.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Packet {
    private Header header;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
