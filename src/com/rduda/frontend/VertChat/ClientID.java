package com.rduda.frontend.VertChat;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Contains client parameters.
 */
class ClientID {
    private String room;
    private String id;
    private String username;
    private boolean authenticated = false;

    public ClientID(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
