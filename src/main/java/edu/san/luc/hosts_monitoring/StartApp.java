package edu.san.luc.hosts_monitoring;

import edu.san.luc.hosts_monitoring.test.PingTest;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Integer.valueOf;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Created by sanya on 14.09.15.
 */
public class StartApp {

    public static final String DEFAULT_EXECUTOR_POOL_SIZE = "1";

    public static void main(String[] args) {
        new StartApp().start();
    }

    private void start() {
        try {
            Properties appProperties = new Properties();
            appProperties.load(loadStreamFromClasspath("app.properties"));
            int pingTestPoolSize = valueOf(appProperties.getProperty("ping.test.executor.pool.size", DEFAULT_EXECUTOR_POOL_SIZE));
            int responseTestPoolSize = valueOf(appProperties.getProperty("response.test.executor.pool.size", DEFAULT_EXECUTOR_POOL_SIZE));

            ScheduledExecutorService pingTestExecutor = newScheduledThreadPool(pingTestPoolSize);
            ExecutorService responseTestExecutor = newFixedThreadPool(responseTestPoolSize);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<URL> loadUrls() throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        Scanner scanner = new Scanner(loadStreamFromClasspath("hosts.list"));

        while(scanner.hasNextLine()){
            String urlLine = scanner.nextLine();
            urls.add(new URL(urlLine));
        }

        return urls;
    }

    private List<PingTest> createTests(List<URL> urls, ExecutorService responseTestExecutor){
        List<PingTest> tests = new ArrayList<PingTest>();

        for (URL url : urls) {
            PingTest pingTest = new PingTest(url);
            pingTest.setResponseTestExecutorService(responseTestExecutor);
        }

        return tests;
    }

    private void runTests(List<PingTest> pingTests, ExecutorService pingTestExecutor){
        pingTestExecutor.invokeAll()
    }

    private InputStream loadStreamFromClasspath(String filename){
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
}
