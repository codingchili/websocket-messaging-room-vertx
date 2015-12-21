package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.Serializer;
import com.websocket.frontend.room.Protocol.ServerEvent;
import io.vertx.core.Vertx;

/**
 * Created by Robin on 2015-12-19.
 * <p>
 * Provides a delta-buffer for downscaling to avoid fluctuation
 * and prevent fragmentation.
 * <p>
 * todo should monitor memory/cpu/networking.
 */
class LoadManager {
    private Vertx vertx;
    private Boolean full = false;

    public LoadManager(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Evaluates whether the server should notify the system to
     * stop route users to it. This is done during heavy load.
     *
     * @param users Number of currently connected users.
     */
    public void manage(Integer users) {
        if (Configuration.LOAD_MAX_USERS <= users && !full) {
            sendBus(Configuration.NOTIFY(), new ServerEvent(ServerEvent.ServerStatus.FULL));
            full = true;
        }

        if (users <= Configuration.LOAD_MAX_USERS - Configuration.LOAD_DELTA_BUFFER && full) {
            sendBus(Configuration.NOTIFY(), new ServerEvent(ServerEvent.ServerStatus.READY));
            full = false;
        }
    }

    private void sendBus(String address, Object message) {
        vertx.eventBus().send(address, Serializer.pack(message));
    }
}
