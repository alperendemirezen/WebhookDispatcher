package hook;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TestServer {

    public static ArrayList<String> list = new ArrayList<String>();


    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9095), 0);
        server.createContext("/", new WebhookHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Test Webhook Server started at http://localhost:9095/");

    }

    static class WebhookHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream body = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[1024];
                int bytesRead;
                while ((bytesRead = body.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesRead);
                }
                String payload = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                list.add(payload);
                System.out.println(list.size());
                System.out.println("Incoming POST: " + payload);

            } else {
                System.out.println("Received non-POST request");
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }
    }
}
