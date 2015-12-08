package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;

import java.util.concurrent.Future;

/**
 * Created by sanya on 16.09.15.
 */
public class HttpStatusTestRunner implements HostTestRunner<Integer> {
    private HostTest httpStatusTest;
    private RunnerPool runnerPool;

    public HttpStatusTestRunner(HostTest httpStatusTest) {
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

    public void setRunnerPool(RunnerPool runnerPool) {
        this.runnerPool = runnerPool;
    }
}
