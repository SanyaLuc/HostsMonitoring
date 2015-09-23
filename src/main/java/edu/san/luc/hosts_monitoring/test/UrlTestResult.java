package edu.san.luc.hosts_monitoring.test;

import java.net.URL;

/**
 * Created by sanya on 18.09.15.
 */
public final class UrlTestResult {
    public static boolean PING_OK = true;
    public static boolean PING_FAILED = false;

    public static int HTTP_OK = 200;
    public static int HTTP_NOT_FOUND = 404;

    private URL url;
    private Boolean pingStatus;
    private Integer httpStatus;

    public UrlTestResult(URL url, Boolean pingStatus, Integer httpStatus) {
        this.url = url;
        this.pingStatus = pingStatus;
        this.httpStatus = httpStatus;
    }

    public URL getUrl() {
        return url;
    }

    public Boolean getPingStatus() {
        return pingStatus;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
