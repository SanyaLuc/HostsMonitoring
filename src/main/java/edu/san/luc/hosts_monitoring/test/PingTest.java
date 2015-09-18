package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * Created by sanya on 16.09.15.
 */
public class PingTest implements Runnable {

    private URL url;
    private ExecutorService responseTestExecutorService;
    private ExecutorService pingTestExecutorService;

    public PingTest(URL url) {
        this.url = url;
    }

    public void setResponseTestExecutorService(ExecutorService responseTestExecutorService) {
        this.responseTestExecutorService = responseTestExecutorService;
    }

    public void setPingTestExecutorService(ExecutorService pingTestExecutorService) {
        this.pingTestExecutorService = pingTestExecutorService;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public void run() {
        this.responseTestExecutorService.submit(new HttpStatusTest());
    }

    public void sumbmit(){
        this.pingTestExecutorService.submit(this);
    }

}


