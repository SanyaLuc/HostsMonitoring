package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class SimpleThreadPingTestRunner extends AbstractPingTestRunner implements Runnable {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private ArrayBlockingQueue<Thread> threadPool;
    private SimpleFuture<HostTestResult> future;

    public SimpleThreadPingTestRunner(HostTest pingTest) {
        super(pingTest);
    }

    public Future<HostTestResult> submit(){
        Thread t = new Thread(this);
        future = new SimpleFuture(t);

        t.start();

        return future;
    }

    @Override
    public void run() {
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            try {
                for (;;) {
                    if(future == null)
                        throw new IllegalStateException();
                    try {
                        HostTestResult result = call();
                        future.setResult(result);

                        int delay = intervalPerPingStatus.get(result.getPingStatus());
                        available.await(delay, SECONDS);
                    } catch (Exception e) {
                        future.setException(e);
                    }
                }
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setThreadPool(ArrayBlockingQueue<Thread> threadPool) {
        this.threadPool = threadPool;
    }

    public void setTestResults(Map<String, HostTestResult> testResults) {
        this.testResults = testResults;
    }

    public void setIntervalPerPingStatus(Map<Boolean,Integer> intervalPerPingStatus) {
        this.intervalPerPingStatus = intervalPerPingStatus;
    }
}


