package edu.san.luc.hosts_monitoring.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Created by sanya on 15.09.15.
 */
public class HostMonitoringWebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Web Server is started!");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<html>" +
                    "<header><title>URL Status Report</title></header>" +
                    "<body>" +
                    "<table>" +
                        "<tr><td>1</td><td>yandex.com</td></tr>"+
                        "<tr><td>2</td><td>google.com</td></tr>"+
                    "</table>" +
                    "</body>" +
                    "</html>";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
