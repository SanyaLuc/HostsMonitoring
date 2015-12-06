package edu.san.luc.hosts_monitoring;

import edu.san.luc.hosts_monitoring.runner.*;
import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;
import edu.san.luc.hosts_monitoring.test.RandomHttpStatusTest;
import edu.san.luc.hosts_monitoring.test.RandomPingTest;
import edu.san.luc.hosts_monitoring.web.HostMonitoringWebServer;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static edu.san.luc.hosts_monitoring.test.HostTestResult.PING_FAILED;
import static edu.san.luc.hosts_monitoring.test.HostTestResult.PING_OK;
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

    private SimpleRunnerPool pingTestRunnerPool;
    private SimpleRunnerPool httpStatusTestRunnerPool;
    private StandardRunnerPool pingTestExecutor;
    private StandardRunnerPool httpStatusTestExecutor;

    private List<HostTestRunner> pingTests;

    private List<URL> urls;
    private Map<String, HostTestResult> sharedTestResults;
    private Properties appProperties;
    private Integer pingTestPoolSize;
    private Integer responseTestPoolSize;

    public HostMonitoringApp() {
        init();
    }

    public static void main(String[] args) throws Exception {
        HostMonitoringApp app = new HostMonitoringApp();

        HostMonitoringWebServer webServer = new HostMonitoringWebServer();
        webServer.setUrls(app.urls);
        webServer.setTestResults(app.sharedTestResults);

        app.runTests();
        webServer.start();
    }

    public void init() {
        try {
            appProperties = new Properties();
            appProperties.load(loadStreamFromClasspath("app.properties"));

            pingTestPoolSize = valueOf(appProperties.getProperty("ping.test.executor.pool.size", DEFAULT_EXECUTOR_POOL_SIZE));
            responseTestPoolSize = valueOf(appProperties.getProperty("http.status.test.executor.pool.size", DEFAULT_EXECUTOR_POOL_SIZE));

            pingTestExecutor = new StandardRunnerPool(pingTestPoolSize);
            httpStatusTestExecutor = new StandardRunnerPool(responseTestPoolSize);

            pingTestRunnerPool = new SimpleRunnerPool(pingTestPoolSize);
            httpStatusTestRunnerPool = new SimpleRunnerPool(responseTestPoolSize);

            urls = loadUrls();

            sharedTestResults = createInitialTestResults(urls);

            pingTests = createTests(urls, PingTestRunner.class);
//            pingTests = createTests(urls, SimpleThreadPingTestRunner.class);
        } catch (Exception e) {
            throw new AppInitializingException("Couldn't initialize the app", e);
        }
    }

    private List<URL> loadUrls() throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        Scanner scanner = new Scanner(loadStreamFromClasspath("hosts.list"));

        while (scanner.hasNextLine()) {
            String urlLine = scanner.nextLine().trim();
            urls.add(new URL(urlLine));
        }

        return urls;
    }

    private List<HostTestRunner> createTests(List<URL> urls, Class<? extends PingTestRunner> type) {
        Map<String, HostTestRunner> groupedTests = new LinkedHashMap<String, HostTestRunner>(urls.size());

        Map<Boolean, Integer> intervalPerPingStatus = mapIntervalPerPingStatus();

        for (URL url : urls) {
            HostTest pingTest = new RandomPingTest(url);
            PingTestRunner pingTestRunner = new PingTestRunner(pingTest);
            pingTestRunner.setIntervalPerPingStatus(intervalPerPingStatus);
            pingTestRunner.setTestResults(sharedTestResults);

            RandomHttpStatusTest httpStatusTest = new RandomHttpStatusTest(url);
            HttpStatusTestRunner httpStatusTestRunner = new HttpStatusTestRunner(httpStatusTest);
            pingTestRunner.setHttpStatusTestRunner(httpStatusTestRunner);

            if(type == PingTestRunner.class){
                pingTestRunner.setRunnerPool(pingTestExecutor);
                httpStatusTestRunner.setRunnerPool(httpStatusTestExecutor);
            } else {
                pingTestRunner.setRunnerPool(pingTestRunnerPool);
                httpStatusTestRunner.setRunnerPool(httpStatusTestRunnerPool);
            }

            String host = url.getHost();

            String parentHost = host.substring(host.indexOf(".") + 1);
            PingTestRunner parentTest = (PingTestRunner) groupedTests.get(parentHost);

            if (parentTest != null)
                parentTest.addSubTest(pingTestRunner);
            else
                groupedTests.put(host, pingTestRunner);
        }

        return new ArrayList<>(groupedTests.values());
    }

    private void runTests() {
        for (HostTestRunner pingTest : pingTests) {
            pingTest.start();
        }
    }

    private Map<Boolean, Integer> mapIntervalPerPingStatus() {
        final int pingFailureInterval = valueOf(appProperties.getProperty("ping.failure.retry.interval", DEFAULT_PING_INTERVAL));
        final int pingSuccessInterval = valueOf(appProperties.getProperty("ping.success.retry.interval", DEFAULT_PING_INTERVAL));

        return new HashMap<Boolean, Integer>() {{
            put(PING_FAILED, pingFailureInterval);
            put(PING_OK, pingSuccessInterval);
        }};
    }

    private InputStream loadStreamFromClasspath(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    private Map<String, HostTestResult> createInitialTestResults(List<URL> urls) {
        Map<String, HostTestResult> sharedTestResults = new ConcurrentHashMap<String, HostTestResult>(urls.size());

        for (URL url : urls) {
            sharedTestResults.put(url.getHost(), new HostTestResult(url, null, null));
        }

        return sharedTestResults;
    }
}
