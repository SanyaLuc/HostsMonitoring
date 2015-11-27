package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Random;

/**
 * Created by sanya on 18.11.15.
 */
public class RandomPingTest implements HostTest {
    private Random random = new Random();
    private URL url;

    public RandomPingTest(URL url) {
        this.url = url;
    }

    @Override
    public Integer test() throws Exception {
        System.out.println("ping "+url);
        Thread.sleep(2000);
        return random.nextInt(2);
    }

    @Override
    public URL getURL() {
        return url;
    }
}
