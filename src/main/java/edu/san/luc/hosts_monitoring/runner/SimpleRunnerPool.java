package edu.san.luc.hosts_monitoring.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Created by sanya on 28.11.15.
 */
public class SimpleRunnerPool<R, T extends Callable<R>> {

    private Integer limit;
    private List<QueueWorker> workers;
    private PriorityBlockingQueue<DeferredRunner> queue;

    public SimpleRunnerPool(int limit) {
        this.limit = limit;
        this.workers = new ArrayList<>(limit);
        this.queue = new PriorityBlockingQueue<>(limit);
    }

    public Future<R> submit(T runner) {
        SimpleFuture<R> future = new SimpleFuture<>();
        queue.put(new DeferredRunner(future, runner));

        startWorker();

        return future;
    }

    private synchronized boolean startWorker(){
        if (workers.size() < limit) {
            QueueWorker worker = new QueueWorker();
            workers.add(worker);
            Thread t = new Thread(worker);
            t.start();

            return true;
        }

        return false;
    }

    private class DeferredRunner implements Comparable<DeferredRunner> {
        private SimpleFuture<R> future;
        private T runner;

        private DeferredRunner(SimpleFuture<R> future, T runner) {
            this.future = future;
            this.runner = runner;
        }

        @Override
        public int compareTo(DeferredRunner dr) {
            return future.compareTo(dr.future);
        }
    }

    private class QueueWorker implements Runnable {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition available = lock.newCondition();

        @Override
        public void run() {
            try {
                for (; ; ) {
                    final DeferredRunner deferredRunner = queue.take();
                    final T runner = deferredRunner.runner;
                    final SimpleFuture<R> future = deferredRunner.future;

                    final long delay = future.getDelay();
//                System.out.println("#### "+runner.pingTest.getURL()+" @@@@@ " + Thread.currentThread());
                    if (delay < 0) {
                        try {
                            R result = runner.call();
                            future.setResult(result);
                        } catch (Exception e) {
                            future.setException(e);
                        }
                    } else {
                        lock.lockInterruptibly();
                        try {
                            available.await(delay, NANOSECONDS);
                        } finally {
                            lock.unlock();
                        }
                        queue.put(deferredRunner);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
