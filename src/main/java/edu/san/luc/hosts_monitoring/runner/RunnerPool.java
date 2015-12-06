package edu.san.luc.hosts_monitoring.runner;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanya on 06.12.15.
 */
public interface RunnerPool {
    <T> Future<T> submit(Callable<T> task);

    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, TimeUnit unit);
}
