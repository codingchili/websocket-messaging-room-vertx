package com.websocket.frontend.room.Protocol;

/**
 * Created by Robin on 2015-12-22.
 * <p>
 * A command that may be issued to the server.
 */
public class Command {
    private String name;
    private String parameters;

    public Command() {
    }

    public Command(String name, String parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
