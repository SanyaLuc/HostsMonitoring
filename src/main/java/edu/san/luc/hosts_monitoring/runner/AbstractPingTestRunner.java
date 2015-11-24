package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by sanya on 22.11.15.
 */
public abstract class AbstractPingTestRunner implements HostTestRunner<HostTestResult> {

    protected HostTest pingTest;

    protected List<AbstractPingTestRunner> subTests = new ArrayList<>();
    protected HostTestRunner<Integer> httpStatusTestRunner;

    protected Map<Boolean, Integer> intervalPerPingStatus;

    protected Map<String, HostTestResult> testResults;


    public AbstractPingTestRunner(HostTest pingTest) {
        this.pingTest = pingTest;
    }

    @Override
    public HostTestResult call() throws Exception {
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
                Future<Integer> future = httpStatusTestRunner.submit();
                httpStatus = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            result = updateTestStatus(url, pingStatus, httpStatus);
        }

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

    public void addSubTest(AbstractPingTestRunner subTest) {
        this.subTests.add(subTest);
    }
}
