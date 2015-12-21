package com.websocket.frontend.room;

import com.websocket.frontend.room.Protocol.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Created by Robin on 2015-12-17.
 * <p>
 * Tests the Chat.
 */
@RunWith(VertxUnitRunner.class)
public class ChatVerticleIntegration {
    private Vertx vertx;
    private String USER;
    private String PASS;
    private String ROOM;

    @Rule
    public Timeout timeout = new Timeout(2000);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        USER = UUID.randomUUID().toString();
        PASS = USER;
        ROOM = UUID.randomUUID().toString();
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void connectToWebsocket(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> async.complete(), new ServerList());
    }

    @Test
    public void getServerList(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            Packet packet = (Packet) Serializer.unpack(data.toString(), Packet.class);

            System.out.println(data.toString());

            if (packet.getAction().equals(Message.ACTION)) {
                context.assertTrue(data.toString().contains("AVAILABLE"));
                async.complete();
            }
        }, new ServerList());
    }

    @Test
    public void setRoomTopicNotAuthorized(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            USER += ".1";

            getConnectedSkipHandshake(data2 -> {
                if (data2.toString().contains("Not authorized"))
                    async.complete();

            }, new Topic(null, "topic_new"));
        }, new Message());
    }

    @Test
    public void joinNewRoomBecomesOwner(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            if (data.toString().contains("Room owner '" + USER + "'")) {
                async.complete();
            }
        }, new Join(USER, ""));
    }

    @Test
    public void shouldReturnServerCommands(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            String content = data.toString();

            context.assertTrue(content.contains("/join"));
            context.assertTrue(content.contains("/authenticate"));
            context.assertTrue(content.contains("/connect"));
            context.assertTrue(content.contains("/topic"));
            context.assertTrue(content.contains("/servers"));
            context.assertTrue(content.contains("/help"));

            async.complete();
        }, new Help());
    }

    @Test
    public void joinNewRoomNotLoadedFromCache(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            String content = data.toString();

            if (content.contains("Loading room..")) {
                async.complete();
            }
        }, new Join(USER, ""));
    }

    @Test
    public void joinWhenInsideShouldFail(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            context.assertTrue(data.toString().contains("Already inside room"));
            async.complete();
        }, (new Join(ROOM, "")));
    }

    @Test
    public void messagesBroadcastIntoRoom(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            context.assertTrue(data.toString().contains("hello"));
            async.complete();
        }, new Message("hello"));
    }

    @Test
    public void shouldFailAuthentication(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(onConnect -> getUnconnected(handler -> {
            if (handler.toString().contains("Authentication Required."))
                async.complete();
        }), new Authenticate(USER, "WRONG"));
    }

    @Test
    public void shouldCreateAccount(TestContext context) {
        final Async async = context.async();

        getUnconnected(handler -> {
            Packet packet = (Packet) Serializer.unpack(handler.toString(), Packet.class);

            if (packet.getAction().equals(Authenticate.ACTION)) {
                Authenticate authenticate = (Authenticate) Serializer.unpack(handler.toString(), Authenticate.class);

                if (authenticate.isCreated() && authenticate.isAuthenticated())
                    async.complete();
            }
        }, new Authenticate(USER, PASS));
    }

    @Ignore
    public void shouldChangeTopic(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(handler -> {
            context.assertTrue(handler.toString().contains("NEW_TOPIC"));
            async.complete();
        }, new Join(USER), new Topic("NEW_TOPIC"));
    }

    private void getUnconnected(Handler<Buffer> handler, Object... messages) {
        HttpClient client = vertx.createHttpClient();

        client.websocket(Configuration.LISTEN_PORT, "localhost", "/", event -> {
            event.handler(handler);

            for (Object message : messages) {
                sendBus(event.textHandlerID(), message);
            }
        });
    }

    private void getConnectedSkipHandshake(Handler<Buffer> handler, Object... messages) {
        HttpClient client = vertx.createHttpClient();

        client.websocket(Configuration.LISTEN_PORT, "localhost", "/", event -> {
            event.handler(data -> {
                Packet packet = (Packet) Serializer.unpack(data.toString(), Packet.class);

                System.out.println("get.connected =" + data.toString());

                if (packet.getAction().equals(Authenticate.ACTION)) {
                    sendBus(event.textHandlerID(), new Join(ROOM, "topic"));
                }


                if (packet.getAction().equals(Join.ACTION) && data.toString().contains(ROOM)) {
                    event.handler(handler);

                    System.out.println("action == join && room = ok");

                    for (Object message : messages) {
                        sendBus(event.textHandlerID(), message);
                    }
                }
            });

            sendBus(event.textHandlerID(), new Authenticate(USER, PASS));
        });
    }

    private void sendBus(String address, Object message) {
        vertx.eventBus().send(address, Serializer.pack(message));
    }
}
