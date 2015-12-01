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
public class SimpleThreadPingTestRunner extends AbstractPingTestRunner implements Runnable, Comparable<SimpleThreadPingTestRunner> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();
    private SimpleRunnerPool<SimpleThreadPingTestRunner> runnerPool;
    private SimpleFuture<HostTestResult> future = new SimpleFuture();
    private Long triggerTime = 0L;

    public SimpleThreadPingTestRunner(HostTest pingTest) {
        super(pingTest);
    }

    public Future<HostTestResult> start() {
        if (runnerPool.put(this)) {
            Thread t = new Thread(this);
            t.start();
        }

        return future;
    }

    private void restart() throws Exception {
        HostTestResult result = future.get();
        boolean pingStatus = result != null ? result.getPingStatus() : false;
        int delay = intervalPerPingStatus.get(pingStatus);
        triggerTime = triggerTime(delay);
        runnerPool.put(this);
    }

    private void testHost() {
        try {
            future.setResult(call());
        } catch (Exception e) {
            future.setException(e);
        }
    }

    @Override
    public void run() {
        try {
            lock.lockInterruptibly();
            for (; ; ) {
                final SimpleThreadPingTestRunner runner = runnerPool.take();

                final long delay = runner.getDelay();
//                System.out.println("#### "+runner.pingTest.getURL()+" @@@@@ " + Thread.currentThread());
                if (delay < 0) {
                    runner.testHost();
                    if (runner.barrier == null) {
                        runner.restart();
                    }
                } else {
                    available.await(delay, NANOSECONDS);
                    runnerPool.put(runner);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private static long triggerTime(int delay) {
        return nanoTime() + SECONDS.toNanos(delay);
    }

    private long getDelay() {
        return triggerTime - nanoTime();
    }

    private void takeNewRunner() {
        available.signal();
//        try {
//            lock.lockInterruptibly();
//            //available.signal();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
    }

    public void setRunnerPool(SimpleRunnerPool<SimpleThreadPingTestRunner> runnerPool) {
        this.runnerPool = runnerPool;

        if (runnerPool != null) {
            runnerPool.addPutListener(runner -> takeNewRunner());
        }
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


