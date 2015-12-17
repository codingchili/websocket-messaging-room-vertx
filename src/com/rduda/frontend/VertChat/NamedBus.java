package com.rduda.frontend.VertChat;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Mapping the bus configurations to constants.
 */
class NamedBus {
    public static String NOTIFY() {
        return "notify";
    }

    public static String EVENT() {
        return "event";
    }

    public static String ROOM(String name) {
        return "room." + name;
    }

    public static String ROOMDATA() {
        return "roomdata";
    }

    public static String CLIENT() {
        return "client.";
    }

    public static String CLIENT(String id) {
        return "client." + id;
    }
}
