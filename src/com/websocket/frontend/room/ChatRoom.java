package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.Room;

import java.util.HashMap;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Defines a room, of connected clients and metadata.
 */
class ChatRoom {
    private HashMap<String, ClientID> clients = new HashMap<>();
    private Room settings = new Room();

    public ChatRoom(Room room) {
        this(room.getRoom(), room.getTopic(), room.getOwner());
    }

    public ChatRoom(String name, String topic, String username) {
        settings.setRoom(name);
        settings.setTopic(topic);
        settings.setOwner(username);
    }

    public HashMap<String, ClientID> getClients() {
        return clients;
    }

    public void remove(ClientID client) {
        clients.remove(client.getId());
    }

    public ClientID get(String id) {
        return clients.get(id);
    }

    public void add(ClientID client) {
        clients.put(client.getId(), client);
    }

    public void setSettings(Room settings) {
        this.settings = settings;
    }

    public Room getSettings() {
        return settings;
    }

    public String getOwner() {
        return settings.getOwner();
    }
}
