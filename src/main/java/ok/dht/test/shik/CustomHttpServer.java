package ok.dht.test.shik;

import one.nio.http.HttpServer;
import one.nio.http.HttpServerConfig;
import one.nio.http.HttpSession;
import one.nio.http.Request;
import one.nio.http.Response;
import one.nio.net.Session;
import one.nio.server.SelectorThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class CustomHttpServer extends HttpServer {

    private static final String CLOSE_CONNECTION_HEADER = "Connection: close";
    private static final Log LOG = LogFactory.getLog(ServiceImpl.class);

    public CustomHttpServer(HttpServerConfig config, Object... routers) throws IOException {
        super(config, routers);
    }

    @Override
    public void handleDefault(Request request, HttpSession session) throws IOException {
        Response response = new Response(Response.BAD_REQUEST, Response.EMPTY);
        session.sendResponse(response);
    }

    @Override
    public synchronized void stop() {
        for (SelectorThread selector : selectors) {
            for (Session session : selector.selector) {
                Response response = new Response(Response.OK, Response.EMPTY);
                response.addHeader(CLOSE_CONNECTION_HEADER);
                byte[] responseBytes = response.toBytes(false);
                try {
                    session.write(responseBytes, 0, responseBytes.length);
                } catch (IOException e) {
                    LOG.error("Error while sending client info about closing socket");
                }
                session.socket().close();
            }
        }
        super.stop();
    }

}
