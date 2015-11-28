package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
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
    private BoundedPriorityBlockingQueue<SimpleThreadPingTestRunner> runnerPool;
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
        SimpleThreadPingTestRunner runner = this;
        if(runner.barrier == null){
            final ReentrantLock lock = runner.lock;
            try {
                lock.lockInterruptibly();
                try {
                    for (;;) {
                        testHost();

                        HostTestResult result = runner.future.get();
                        if(result != null){
                            int delay = runner.intervalPerPingStatus.get(result.getPingStatus());
                            runner.available.await(delay, SECONDS);
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            testHost();
        }
    }

    private void testHost(){
        if(future == null)
            throw new IllegalStateException();
        try {
            HostTestResult result = call();
            future.setResult(result);
        } catch (Exception e) {
            future.setException(e);
        }
    }

    public void setRunnerPool(BoundedPriorityBlockingQueue<SimpleThreadPingTestRunner> runnerPool) {
        this.runnerPool = runnerPool;
    }

    public void setTestResults(Map<String, HostTestResult> testResults) {
        this.testResults = testResults;
    }

    public void setIntervalPerPingStatus(Map<Boolean,Integer> intervalPerPingStatus) {
        this.intervalPerPingStatus = intervalPerPingStatus;
    }
}


