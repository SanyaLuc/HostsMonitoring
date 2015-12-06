package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 22.11.15.
 */
public class PingTestRunner implements HostTestRunner<HostTestResult> {

    private HostTest pingTest;

    private List<PingTestRunner> subTestRunners = new ArrayList<>();
    private HostTestRunner<Integer> httpStatusTestRunner;

    private Map<Boolean, Integer> intervalPerPingStatus;

    private Map<String, HostTestResult> testResults;

    private RunnerPool runnerPool;

    //    protected CyclicBarrier barrier;
    protected CountDownLatch barrier;

    public PingTestRunner(HostTest pingTest) {
        this.pingTest = pingTest;
    }

    public Future<HostTestResult> start() {
        return runnerPool.submit(this);
    }

    @Override
    public HostTestResult call() throws Exception {
        testSubTests();

        HostTestResult result = testHost();

        if (barrier == null) {
            int delay = intervalPerPingStatus.get(result.getPingStatus());
            runnerPool.schedule(this, delay, SECONDS);
        }

        return result;
    }

    private void testSubTests() throws Exception {
        if (subTestRunners.isEmpty())
            return;

//        CyclicBarrier barrier = new CyclicBarrier(subTestRunners.size()+1);
        CountDownLatch barrier = new CountDownLatch(subTestRunners.size());

        for (PingTestRunner subTestRunner : subTestRunners) {
            subTestRunner.barrier = barrier;
            subTestRunner.start();
        }

        barrier.await();
    }

    private HostTestResult testHost() throws Exception {
        URL url = pingTest.getURL();

        updateTestStatus(url, null, null);

        int pingMillis = 0;
        try {
            pingMillis = pingTest.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean pingStatus = pingMillis > 0;

        HostTestResult result = updateTestStatus(url, pingStatus, null);
        if (pingStatus && httpStatusTestRunner != null) {
            int httpStatus = 0;
            try {
                Future<Integer> future = httpStatusTestRunner.start();
                httpStatus = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            result = updateTestStatus(url, pingStatus, httpStatus);
        }

        if (barrier != null)
            barrier.countDown();
//            barrier.await();

        return result;
    }

    private HostTestResult updateTestStatus(URL url, Boolean pingStatus, Integer httpStatus) {
        HostTestResult result = new HostTestResult(url, pingStatus, httpStatus);

        if (testResults != null) {
            testResults.put(url.getHost(), result);
        }

        return result;
    }

    public void setHttpStatusTestRunner(HostTestRunner httpStatusTestRunner) {
        this.httpStatusTestRunner = httpStatusTestRunner;
    }

    public void setTestResults(Map<String, HostTestResult> testResults) {
        this.testResults = testResults;
    }

    public void setIntervalPerPingStatus(Map<Boolean, Integer> intervalPerPingStatus) {
        this.intervalPerPingStatus = intervalPerPingStatus;
    }

    public void setRunnerPool(RunnerPool runnerPool) {
        this.runnerPool = runnerPool;
    }

    public void addSubTest(PingTestRunner subTest) {
        this.subTestRunners.add(subTest);
    }
}
