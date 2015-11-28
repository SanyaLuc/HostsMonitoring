package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class SimpleThreadPingTestRunner extends AbstractPingTestRunner implements Runnable, Comparable<SimpleThreadPingTestRunner> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private SimpleRunnerPool<SimpleThreadPingTestRunner> runnerPool;
    private SimpleFuture<HostTestResult> future;

    //dynamic fields
    private Thread currentThread;
    private Long triggerTime;

    public SimpleThreadPingTestRunner(HostTest pingTest) {
        super(pingTest);
    }

    public Future<HostTestResult> submit() {
        if (runnerPool.put(this)) {
            Thread t = new Thread(this);
            t.start();
        }

        future = new SimpleFuture();
        return future;
    }

    private void resubmit() throws Exception {
        HostTestResult result = future.get();
        if (result != null) {
            int delay = intervalPerPingStatus.get(result.getPingStatus());
            triggerTime = triggerTime(delay);
            runnerPool.put(this);
        }
    }

    private void testHost() {
        if (future == null)
            throw new IllegalStateException();
        try {
            HostTestResult result = call();
            future.setResult(result);
        } catch (Exception e) {
            future.setException(e);
        }
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                SimpleThreadPingTestRunner runner = runnerPool.take();

                final long delay = getDelay();
                if (delay < 0) {
                    runner.testHost();
                    if (runner.barrier == null) {
                        runner.resubmit();
                    }
                } else {
                    final ReentrantLock lock = runner.lock;
                    try {
                        lock.lockInterruptibly();
                        runner.available.await(delay, NANOSECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long triggerTime(int delay) {
        return System.nanoTime() + SECONDS.toNanos(delay);
    }

    private long getDelay() {
        return triggerTime - System.nanoTime();
    }

    public void setRunnerPool(SimpleRunnerPool<SimpleThreadPingTestRunner> runnerPool) {
        this.runnerPool = runnerPool;
    }

    public void setTestResults(Map<String, HostTestResult> testResults) {
        this.testResults = testResults;
    }

    public void setIntervalPerPingStatus(Map<Boolean, Integer> intervalPerPingStatus) {
        this.intervalPerPingStatus = intervalPerPingStatus;
    }

    @Override
    public int compareTo(SimpleThreadPingTestRunner runner) {
        return this.triggerTime.compareTo(runner.triggerTime);
    }
}


