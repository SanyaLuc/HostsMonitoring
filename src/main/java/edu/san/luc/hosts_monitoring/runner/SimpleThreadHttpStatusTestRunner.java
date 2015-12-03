package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class SimpleThreadHttpStatusTestRunner implements Runnable, Comparable<SimpleThreadHttpStatusTestRunner>, HostTestRunner<Integer> {
    private HostTest httpStatusTest;
    private SimpleFuture future;
    private SimpleRunnerPool<SimpleThreadHttpStatusTestRunner> runnerPool;

    public SimpleThreadHttpStatusTestRunner(HostTest httpStatusTest) {
        this.httpStatusTest = httpStatusTest;
    }

    @Override
    public Future<Integer> start() {
        future = new SimpleFuture();
        if (runnerPool.put(this)) {
            Thread t = new Thread(this);
            t.start();
        }

        return future;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                final SimpleThreadHttpStatusTestRunner runner = runnerPool.take();
                runner.testHost();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testHost() {
        try {
            future.setResult(call());
        } catch (Exception e) {
            future.setException(e);
        }
    }

    @Override
    public Integer call() throws Exception {
        return httpStatusTest.test();
    }

    public void setRunnerPool(SimpleRunnerPool<SimpleThreadHttpStatusTestRunner> runnerPool) {
        this.runnerPool = runnerPool;
    }

    @Override
    public int compareTo(SimpleThreadHttpStatusTestRunner o) {
        return 0;
    }
}
