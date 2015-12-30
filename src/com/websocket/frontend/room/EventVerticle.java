package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.Register;
import com.websocket.frontend.room.Protocol.Serializer;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;

/**
 * Created by Robin on 2015-12-16.
 * <p>
 * Handles events from the backend and emits events to the backend.
 */
class EventVerticle implements Verticle {
    private static final Integer CONNECTOR_PORT = 5030;
    private Vertx vertx;
    private HttpClient client;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        connectToBackend();
    }

    // todo reconnect must be set so that operation may resume.
    private void connectToBackend() {
        client = vertx.createHttpClient();

        client.websocket(CONNECTOR_PORT, "localhost", "/", event -> {

            // listen for events from the backend connector service.
            event.handler(data -> {
                vertx.eventBus().send(Configuration.DOWNSTREAM, data);
            });

            // forward emitted events onto the connector.
            vertx.eventBus().consumer(Configuration.UPSTREAM, handler -> {
                vertx.eventBus().send(event.textHandlerID(), handler.body().toString());
            });

            // register this server to the connector for events.
            vertx.eventBus().send(event.textHandlerID(),
                    Serializer.pack(new Register(Configuration.REGISTER_NAME, Configuration.LISTEN_PORT + "")));
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        client.close();
    }
}
