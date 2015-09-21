package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class PingTest implements Runnable {

    private URL url;
    private ExecutorService httpStatusTestExecutorService;
    private ScheduledExecutorService pingTestExecutorService;
    private Map<Boolean, Integer> intervalPerPingStatus;
    private Integer pingAttemptsLimit;
    private Integer httpStatusTimeout;
    private Integer pingTimeout;
    private Map<URL, UrlTestResult> testResults;

    private HttpStatusTest httpStatusTest;

    public PingTest(URL url) {
        this.url = url;
        this.httpStatusTest = new HttpStatusTest(url);
    }

    public void sumbmit(){
        pingTestExecutorService.submit(this);
    }

    @Override
    public void run() {
        testResults.put(url, new UrlTestResult(url, null, null));
        int pingMillis = ping();
        int httpStatus = 0;
        boolean pingStatus = pingMillis > 0;

        testResults.put(url, new UrlTestResult(url, pingStatus, null));
        if(pingStatus) {
            Future<Integer> future = httpStatusTestExecutorService.submit(httpStatusTest);
            try {
                httpStatus = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            testResults.put(url, new UrlTestResult(url, pingStatus, httpStatus));
        }

        int delay = intervalPerPingStatus.get(pingStatus);
        pingTestExecutorService.schedule(this, delay, SECONDS);
    }

    private int ping(){
        return new Random().nextInt(1);
    }

    public void setTestResults(Map<URL, UrlTestResult> testResults) {
        this.testResults = testResults;
    }

    public void setHttpStatusTestExecutorService(ExecutorService responseTestExecutorService) {
        this.httpStatusTestExecutorService = responseTestExecutorService;
    }

    public void setPingTestExecutorService(ScheduledExecutorService pingTestExecutorService) {
        this.pingTestExecutorService = pingTestExecutorService;
    }

    public void setIntervalPerPingStatus(Map<Boolean,Integer> intervalPerPingStatus) {
        this.intervalPerPingStatus = intervalPerPingStatus;
    }

    public void setPingAttemptsLimit(Integer pingAttemptsLimit) {
        this.pingAttemptsLimit = pingAttemptsLimit;
    }

    public void setPingTimeout(Integer pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public void setHttpStatusTimeout(Integer httpStatusTimeout) {
        this.httpStatusTimeout = httpStatusTimeout;
    }
}


