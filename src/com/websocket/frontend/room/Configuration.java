package com.websocket.frontend.room;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Mapping the bus channels to constants.
 */
public class Configuration {
    public static final String REGISTER_NAME = "vertx.server.35";
    public final static String SERVER_ROOM = "SYSTEM";
    public final static String DEFAULT_ROOM = "Public";
    public final static String SERVER_NAME = "VERT.Y";
    public final static String SERVER_TOPIC = "Authentication Required";
    public final static Integer LISTEN_PORT = 4035;

    public static final int LOGGER_PORT = 5454;
    public static final String BUS_LOGGER = "logging.upload";
    public static final int LOG_INTERVAL = 1000;

    // maximum number of users before the server emits a FULL message.
    public static final Integer LOAD_MAX_USERS = 2;

    // the number of users which has too leave before advertising READY.
    public static final Integer LOAD_DELTA_BUFFER = 2;

    public static String NOTIFY() {
        return "notify";
    }

    public static String EVENT() {
        return "event";
    }
}
