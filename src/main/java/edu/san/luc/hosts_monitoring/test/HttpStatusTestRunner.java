package edu.san.luc.hosts_monitoring.test;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by sanya on 16.09.15.
 */
public class HttpStatusTestRunner implements HostTestRunner<Integer> {
    private HostTest httpStatusTest;
    private ExecutorService httpStatusTestExecutor;

    public HttpStatusTestRunner(HostTest httpStatusTest) {
        this.httpStatusTest = httpStatusTest;
    }

    @Override
    public Future<Integer> submit() {
        return httpStatusTestExecutor.submit(this);
    }

    @Override
    public Integer call() throws Exception {
        return httpStatusTest.test();
    }

    public void setHttpStatusTestExecutor(ExecutorService httpStatusTestExecutor) {
        this.httpStatusTestExecutor = httpStatusTestExecutor;
    }
}
