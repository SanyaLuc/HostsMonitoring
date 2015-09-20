package edu.san.luc.hosts_monitoring;

import edu.san.luc.hosts_monitoring.test.PingTest;
import edu.san.luc.hosts_monitoring.test.UrlTestResult;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Integer.valueOf;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Created by sanya on 14.09.15.
 */
public class HostMonitoringApp {

    public static final String DEFAULT_EXECUTOR_POOL_SIZE = "1";
    public static final String DEFAULT_ATTEMPTS_LIMIT = "1";
    public static final String DEFAULT_TIMEOUT = "1000";
    public static final String DEFAULT_PING_INTERVAL = "60";

    private Properties appProperties;
    private ScheduledExecutorService pingTestExecutor;
    private ExecutorService httpStatusTestExecutor;
    private List<PingTest> pingTests;

    public HostMonitoringApp(){
        init();
    }

    public static void main(String[] args) {
        new HostMonitoringApp().runTests();


    }

    public void init() {
        try {
            appProperties = new Properties();
            appProperties.load(loadStreamFromClasspath("app.properties"));

            int pingTestPoolSize = valueOf(appProperties.getProperty("ping.test.executor.pool.size", DEFAULT_EXECUTOR_POOL_SIZE));
            int responseTestPoolSize = valueOf(appProperties.getProperty("http.status.test.executor.pool.size", DEFAULT_EXECUTOR_POOL_SIZE));

            pingTestExecutor = newScheduledThreadPool(pingTestPoolSize);
            httpStatusTestExecutor = newFixedThreadPool(responseTestPoolSize);

            pingTests = createTests(loadUrls());
        } catch (Exception e) {
            throw new AppInitializingException("Couldn't initialize the app", e);
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

    private List<PingTest> createTests(List<URL> urls){
        List<PingTest> tests = new ArrayList<PingTest>();

        Map<Integer, Integer> intervalPerPingStatus = mapIntervalPerPingStatus();
        int pingAttemptsLimit = valueOf(appProperties.getProperty("ping.attepmts.limit", DEFAULT_PING_INTERVAL));
        int pingTimeout = valueOf(appProperties.getProperty("ping.timeout", DEFAULT_PING_INTERVAL));
        int httpStatusTimeout = valueOf(appProperties.getProperty("http.status.timeout", DEFAULT_PING_INTERVAL));


        for (URL url : urls) {
            PingTest pingTest = new PingTest(url);
            pingTest.setHttpStatusTestExecutorService(httpStatusTestExecutor);
            pingTest.setPingTestExecutorService(pingTestExecutor);
            pingTest.setIntervalPerPingStatus(intervalPerPingStatus);
            pingTest.setPingAttemptsLimit(pingAttemptsLimit);
            pingTest.setPingTimeout(pingTimeout);
            pingTest.setHttpStatusTimeout(httpStatusTimeout);
        }

        return tests;
    }

    private void runTests(){
        for (PingTest pingTest : pingTests) {
            pingTest.sumbmit();
        }
    }

    private Map<Integer, Integer> mapIntervalPerPingStatus(){
        final int pingFailureInterval = valueOf(appProperties.getProperty("ping.failure.retry.interval", DEFAULT_PING_INTERVAL));
        final int pingSuccessInterval = valueOf(appProperties.getProperty("ping.success.retry.interval", DEFAULT_PING_INTERVAL));

        return new HashMap<Integer, Integer>(){{
            put(UrlTestResult.PING_FAILED, pingFailureInterval);
            put(UrlTestResult.PING_OK, pingSuccessInterval);
        }};
    }

    private InputStream loadStreamFromClasspath(String filename){
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
}
