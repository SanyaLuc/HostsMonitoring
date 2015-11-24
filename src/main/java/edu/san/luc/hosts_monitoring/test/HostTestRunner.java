package edu.san.luc.hosts_monitoring.test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by sanya on 13.11.15.
 */
public interface HostTestRunner<T> extends Callable<T> {
    Future<T> submit();
}
