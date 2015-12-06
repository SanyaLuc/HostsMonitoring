package edu.san.luc.hosts_monitoring.runner;

import java.util.concurrent.*;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 22.11.15.
 */
public class SimpleFuture<T> implements ScheduledFuture<T> {
    private T result;
    private Exception exception;
    private Long triggerTime = 0L;

    public SimpleFuture() {
    }

    public SimpleFuture(int delay) {
        this.triggerTime = triggerTime(delay);
    }

    private static long triggerTime(int delay) {
        return nanoTime() + SECONDS.toNanos(delay);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = triggerTime - nanoTime();
        if (unit == NANOSECONDS)
            return delay;
        else
            return unit.convert(delay, NANOSECONDS);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return exception != null || result != null;
    }

    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        for (; ; ) {
            if (isDone()) {
                if (exception != null) {
                    if (exception instanceof InterruptedException)
                        throw (InterruptedException) exception;
                    else
                        throw new ExecutionException(exception);
                } else {
                    return result;
                }
            } else {
                this.wait();
            }
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }

    public synchronized void setResult(T result) {
        this.result = result;
        this.notifyAll();
    }

    public synchronized void setException(Exception exception) {
        this.exception = exception;
        this.notifyAll();
    }

    @Override
    public int compareTo(Delayed d) {
        return new Long(this.getDelay(NANOSECONDS)).compareTo(d.getDelay(NANOSECONDS));
    }
}
