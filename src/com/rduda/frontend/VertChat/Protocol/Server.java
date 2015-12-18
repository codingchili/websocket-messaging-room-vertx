package com.rduda.frontend.VertChat.Protocol;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * A host running the room protocol.
 */
public class Server {
    private String ip;
    private String name;
    private String port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
