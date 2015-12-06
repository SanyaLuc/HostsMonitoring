package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Random;

/**
 * Created by sanya on 18.11.15.
 */
public class RandomHttpStatusTest implements HostTest {
    private Random random = new Random();
    private URL url;

    public RandomHttpStatusTest(URL url) {
        this.url = url;
    }

    @Override
    public Integer test() throws Exception {
        Thread.sleep(2000);
        return 200 + random.nextInt(200);
    }

    @Override
    public URL getURL() {
        return url;
    }
}
