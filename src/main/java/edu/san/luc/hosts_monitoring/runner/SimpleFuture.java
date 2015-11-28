package edu.san.luc.hosts_monitoring.runner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by sanya on 22.11.15.
 */
public class SimpleFuture<T> implements Future<T> {
    private T result;
    private Exception exception;

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
        for(;;){
            if(isDone()){
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
}
