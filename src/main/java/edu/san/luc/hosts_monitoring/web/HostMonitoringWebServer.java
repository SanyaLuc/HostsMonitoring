package edu.san.luc.hosts_monitoring.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.san.luc.hosts_monitoring.test.UrlTestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;

/**
 * Created by sanya on 15.09.15.
 */
public class HostMonitoringWebServer {
    private Map<URL, UrlTestResult> testResults;

    public void setTestResults(Map<URL, UrlTestResult> testResults) {
        this.testResults = testResults;
    }

    public static void main(String[] args) throws Exception {
        new HostMonitoringWebServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Web Server is started!");
    }

    class MyHandler implements HttpHandler {

        public static final String STATUS_ROW_TEMPLATE = "<tr><td> %s </td><td> %s </td><td> %s </td></tr>";

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<html>" +
                    "<header><title>URL Status Report</title></header>" +
                    "<body>" +
                    "<table>" +
                        renderTestStatusTable() +
                    "</table>" +
                    "</body>" +
                    "</html>";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String renderTestStatusTable(){
            StringBuilder tableBody = new StringBuilder();

            for (UrlTestResult urlTestResult : testResults.values()) {
                String url = urlTestResult.getUrl().toString();
                String pingStatus = renderPingStatus(urlTestResult.getPingStatus());
                String httpStatus = renderPingStatus(urlTestResult.getPingStatus());

                tableBody.append(String.format(STATUS_ROW_TEMPLATE, url, pingStatus, httpStatus));
            }
            return tableBody.toString();
        }

        private String renderPingStatus(Boolean pingStatus){
            if(pingStatus == null)
                return "ONGOING";

            return pingStatus ? "OK" : "FAILED";
        }

        private String renderHttpStatus(Integer httpStatus){
            if(httpStatus == null)
                return "ONGOING";

            return httpStatus.toString();
        }
    }

}
