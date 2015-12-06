package edu.san.luc.hosts_monitoring.runner;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 28.11.15.
 */
public class SimpleRunnerPool implements RunnerPool {

    private Integer limit;
    private List<QueueWorker> workers;
    private PriorityBlockingQueue<DeferredRunner> queue;

    public SimpleRunnerPool(int limit) {
        this.limit = limit;
        this.workers = new CopyOnWriteArrayList<>();
        this.queue = new PriorityBlockingQueue<>(limit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> runner) {
        return schedule(runner, 0, SECONDS);
    }

    @Override
    public <T> ScheduledFuture<T> schedule(Callable<T> runner, long delay, TimeUnit unit) {
        SimpleFuture<T> future = delay > 0
                ? new SimpleFuture<>((int)unit.toSeconds(delay))
                : new SimpleFuture<>();
        queue.put(new DeferredRunner(future, runner));

        startWorker();

        notifyWorkers();

        return future;
    }

    private void notifyWorkers() {
        for (QueueWorker worker : workers) {
            try {
                worker.lock.lockInterruptibly();
                try {
                    worker.available.signalAll();
                } finally {
                    worker.lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized boolean startWorker() {
        QueueWorker worker = null;

        synchronized (workers) {
            if (workers.size() < limit) {
                worker = new QueueWorker();
                workers.add(worker);
            }
        }

        if (worker != null) {
            Thread t = new Thread(worker);
            t.start();
        }

        return worker != null;
    }

    private class DeferredRunner implements Comparable<DeferredRunner> {
        private SimpleFuture future;
        private Callable runner;

        private DeferredRunner(SimpleFuture future, Callable runner) {
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
                    final Callable runner = deferredRunner.runner;
                    final SimpleFuture future = deferredRunner.future;

                    final long delay = future.getDelay(NANOSECONDS);
//                System.out.println("#### "+runner.pingTest.getURL()+" @@@@@ " + Thread.currentThread());
                    if (delay < 0) {
                        try {
                            Object result = runner.call();
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
