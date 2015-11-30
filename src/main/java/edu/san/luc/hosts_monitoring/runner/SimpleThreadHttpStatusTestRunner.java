package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;

import java.util.concurrent.*;

/**
 * Created by sanya on 16.09.15.
 */
public class SimpleThreadHttpStatusTestRunner implements Runnable, HostTestRunner<Integer> {
    private HostTest httpStatusTest;
    private SimpleFuture future;
    private ArrayBlockingQueue<Thread> threadPool;

    public SimpleThreadHttpStatusTestRunner(HostTest httpStatusTest) {
        this.httpStatusTest = httpStatusTest;
    }

    @Override
    public Future<Integer> submit() {
        Thread t = new Thread(this);
        future = new SimpleFuture();

        t.start();

        return future;
    }

    @Override
    public void run() {
        if(future == null)
            throw new IllegalStateException();

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

    public void setThreadPool(ArrayBlockingQueue<Thread> threadPool) {
        this.threadPool = threadPool;
    }
}
