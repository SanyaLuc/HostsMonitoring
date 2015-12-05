package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class SimpleThreadPingTestRunner extends AbstractPingTestRunner {
    private SimpleRunnerPool<HostTestResult, SimpleThreadPingTestRunner> runnerPool;

    public SimpleThreadPingTestRunner(HostTest pingTest) {
        super(pingTest);
    }

    public Future<HostTestResult> start() {
        return runnerPool.submit(this);
    }

    @Override
    public HostTestResult call() throws Exception {
        HostTestResult result = super.call();

        if(barrier == null){
            int delay = intervalPerPingStatus.get(result.getPingStatus());
            runnerPool.schedule(delay, this);
        }

        return result;
    }

    public void setRunnerPool(SimpleRunnerPool<HostTestResult, SimpleThreadPingTestRunner> runnerPool) {
        this.runnerPool = runnerPool;
    }
}


