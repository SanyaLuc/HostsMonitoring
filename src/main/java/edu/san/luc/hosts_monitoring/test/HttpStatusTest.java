package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by sanya on 16.09.15.
 */
public class HttpStatusTest implements Callable<Integer> {
    private URL url;

    private Random random = new Random();

    public HttpStatusTest(URL url) {
        this.url = url;
    }

    @Override
    public Integer call() throws Exception {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 200+random.nextInt(200);
    }

}
