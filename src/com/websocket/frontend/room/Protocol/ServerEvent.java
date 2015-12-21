package com.websocket.frontend.room.Protocol;


import com.websocket.frontend.room.Configuration;

/**
 * Created by Robin on 2015-12-18.
 * <p>
 * Sent from the connector, indicating a chatservers state.
 */
public class ServerEvent {
    private String name;
    private String ip;
    private Integer port;
    private Header header;
    private ServerStatus status;

    public ServerEvent() {
        this(null);
    }


    public ServerEvent(ServerStatus status) {
        this.status = status;
        this.name = Configuration.REGISTER_NAME;
        this.header = new Header("registry.server");
    }

    public String getName() {
        return name;
    }

    public ServerEvent setName(String name) {
        this.name = name;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public ServerEvent setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ServerEvent setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Header getHeader() {
        return header;
    }

    public ServerEvent setHeader(Header header) {
        this.header = header;
        return this;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public ServerEvent setStatus(ServerStatus status) {
        this.status = status;
        return this;
    }

    public enum ServerStatus {
        UP, DOWN, FULL, READY
    }

    ;

}
