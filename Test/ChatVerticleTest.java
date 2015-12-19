import com.rduda.frontend.VertChat.Configuration;
import com.rduda.frontend.VertChat.Launcher;
import com.rduda.frontend.VertChat.Protocol.*;
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
public class ChatVerticleTest {
    private Vertx vertx;
    private String USER;
    private String PASS;

    @Rule
    public Timeout timeout = new Timeout(2000);

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        USER = UUID.randomUUID().toString();
        PASS = USER;
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
            if (data.toString().contains("VERT.X"))
                async.complete();
        }, new ServerList());
    }

    @Test
    public void setRoomTopicNotAuthorized(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            if (data.toString().contains("Not authorized"))
                async.complete();
        }, new Topic(null, "topic_new"));
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
        }, (new Join("General", "")));
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
            if (handler.toString().contains("Registered account"))
                async.complete();
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
                sendBus(event.textHandlerID(), Serializer.pack(message));
            }
        });
    }

    private void getConnectedSkipHandshake(Handler<Buffer> handler, Object... messages) {
        HttpClient client = vertx.createHttpClient();

        client.websocket(Configuration.LISTEN_PORT, "localhost", "/", event -> {
            event.handler(data -> {

                if (data.toString().contains("/authenticate")) {
                    sendBus(event.textHandlerID(), Serializer.pack(new Authenticate(USER, PASS)));
                }

                if (data.toString().contains(USER + " has joined the room")) {
                    event.handler(handler);

                    for (Object message : messages)
                        sendBus(event.textHandlerID(), Serializer.pack(message));
                }
            });
        });
    }

    private void sendBus(String address, String message) {
        vertx.eventBus().send(address, message);
    }
}
