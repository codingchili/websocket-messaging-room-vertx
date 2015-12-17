package com.rduda.frontend.VertChat;

import com.rduda.frontend.VertChat.ChatVerticle;
import com.rduda.frontend.VertChat.EventVerticle;
import io.vertx.core.*;

/**
 * Created by Robin on 2015-12-16.
 * <p/>
 * Communicates with the client through a set of websockets.
 */
public class ChatServer implements Verticle {
    private Vertx vertx;

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
    }

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
        vertx.deployVerticle(new ChatVerticle());
        vertx.deployVerticle(new EventVerticle());
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {

    }
}
