package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Created by sanya on 16.09.15.
 */
public class HttpStatusTest implements Callable<Integer> {
    private URL url;

    public HttpStatusTest(URL url) {
        this.url = url;
    }

    @Override
    public Integer call() throws Exception {
        return 200+new Random().nextInt(200);
    }

}
