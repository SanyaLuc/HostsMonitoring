package edu.san.luc.hosts_monitoring.runner;

import edu.san.luc.hosts_monitoring.test.HostTest;
import edu.san.luc.hosts_monitoring.test.HostTestResult;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by sanya on 16.09.15.
 */
public class PingTestRunner extends AbstractPingTestRunner {

    private ScheduledExecutorService pingTestExecutorService;

    public PingTestRunner(HostTest pingTest) {
        super(pingTest);
    }

    public Future<HostTestResult> start() {
        return pingTestExecutorService.submit(this);
    }

    @Override
    public HostTestResult call() throws Exception {
        HostTestResult result = super.call();

        if(barrier == null){
            int delay = intervalPerPingStatus.get(result.getPingStatus());
            pingTestExecutorService.schedule(this, delay, SECONDS);
        }

        return result;
    }

    public void setPingTestExecutorService(ScheduledExecutorService pingTestExecutorService) {
        this.pingTestExecutorService = pingTestExecutorService;
    }
}


