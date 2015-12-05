package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class SimpleThreadHttpStatusTestRunner implements HostTestRunner<Integer> {
    private HostTest httpStatusTest;
    private SimpleRunnerPool<Integer, SimpleThreadHttpStatusTestRunner> runnerPool;

    public SimpleThreadHttpStatusTestRunner(HostTest httpStatusTest) {
        this.httpStatusTest = httpStatusTest;
    }

    @Override
    public Future<Integer> start() {
        return runnerPool.submit(this);
    }

    @Override
    public Integer call() throws Exception {
        return httpStatusTest.test();
    }

    public void setRunnerPool(SimpleRunnerPool<Integer, SimpleThreadHttpStatusTestRunner> runnerPool) {
        this.runnerPool = runnerPool;
    }
}
