package edu.san.luc.hosts_monitoring.runner;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by sanya on 06.12.15.
 */
public class StandardRunnerPool extends ScheduledThreadPoolExecutor implements RunnerPool {
    public StandardRunnerPool(int corePoolSize) {
        super(corePoolSize);
    }
}
