import com.rduda.frontend.VertChat.ChatVerticle;
import com.rduda.frontend.VertChat.Protocol.Message;
import com.rduda.frontend.VertChat.Protocol.Serializer;
import com.rduda.frontend.VertChat.Protocol.ServerList;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

/**
 * Created by Robin on 2015-12-17.
 */
@RunWith(VertxUnitRunner.class)
public class ChatVerticleTest {
    private Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(2500);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();

//        vertx.deployVerticle(new ChatVerticle(),
        //               context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close();
        //    vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void connectToWebsocket(TestContext context) {
        final Async async = context.async();

        getConnectedSkipHandshake(data ->
                async.complete()
                , new ServerList());
    }

    @Test
    public void getServerList(TestContext context) {
        System.out.println("WORKING");
        final Async async = context.async();

        getConnectedSkipHandshake(data -> {
            System.out.println("new handler = " + Serializer.unpack(data.toString(), Message.class));
            async.complete();
        }, new ServerList());
    }

    private void getConnectedSkipHandshake(Handler<Buffer> handler, ServerList message) {
        HttpClient client = vertx.createHttpClient();

        client.websocket(ChatVerticle.LISTEN_PORT, "localhost", "/", event -> {

            event.handler(data -> {

                System.out.println(data.toString());

                if (data.toString().contains("/authenticate")) {
                    event.handler(handler);
                }
            });

            event.writeFinalTextFrame(Serializer.pack(message));
        });
    }
}
