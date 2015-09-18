package edu.san.luc.hosts_monitoring.test;

import java.net.URL;

/**
 * Created by sanya on 18.09.15.
 */
public final class UrlTestResult {
    public static int PING_OK = 0;
    public static int PING_FAILED = 1;

    public static int HTTP_OK = 200;
    public static int HTTP_NOT_FOUND = 404;

    private URL url;
    private int pingStatus;
    private int httpStatus;

    public UrlTestResult(URL url, int pingResult, int httpStatus) {
        this.url = url;
        this.pingStatus = pingResult;
        this.httpStatus = httpStatus;
    }

    public URL getUrl() {
        return url;
    }

    public int getPingStatus() {
        return pingStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
