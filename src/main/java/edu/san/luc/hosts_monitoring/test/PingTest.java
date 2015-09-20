package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by sanya on 16.09.15.
 */
public class PingTest implements Runnable {

    private URL url;
    private ExecutorService responseTestExecutorService;
    private ExecutorService pingTestExecutorService;
    private Map<Integer, Integer> intervalPerPingStatus;
    private Integer pingAttemptsLimit;
    private Integer httpStatusTimeout;
    private Integer pingTimeout;
    private Map<URL, UrlTestResult> testResults;

    public PingTest(URL url) {
        this.url = url;
    }

    public void sumbmit(){
        this.pingTestExecutorService.submit(this);
    }

    @Override
    public void run() {
        this.responseTestExecutorService.submit(new HttpStatusTest());
    }

    public void setTestResults(Map<URL, UrlTestResult> testResults) {
        this.testResults = testResults;
    }

    public void setHttpStatusTestExecutorService(ExecutorService responseTestExecutorService) {
        this.responseTestExecutorService = responseTestExecutorService;
    }

    public void setPingTestExecutorService(ExecutorService pingTestExecutorService) {
        this.pingTestExecutorService = pingTestExecutorService;
    }

    public void setIntervalPerPingStatus(Map<Integer,Integer> intervalPerPingStatus) {
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


