package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.Protocol.Register;
import com.rduda.frontend.VertChat.Protocol.Serializer;
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
public class EventVerticle implements Verticle {
    private static final Integer CONNECTOR_PORT = 5050;
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

    private void connectToBackend() {
        client = vertx.createHttpClient();

        client.websocketStream(CONNECTOR_PORT, "localhost", "/").handler(event -> {
            event.handler(data -> {
                System.out.println("Received " + data);
                vertx.eventBus().send(NamedBus.EVENT(), data);
            });

            vertx.eventBus().consumer(NamedBus.NOTIFY(), handler -> {
                System.out.println("sending " + handler.body());
                event.writeFinalTextFrame((String) handler.body());
            });

            event.writeFinalTextFrame(Serializer.pack(new Register("VERT.X", ChatVerticle.LISTEN_PORT + "")));
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        client.close();
    }
}
