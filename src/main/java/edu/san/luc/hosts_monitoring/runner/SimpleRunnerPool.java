package edu.san.luc.hosts_monitoring.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by sanya on 28.11.15.
 */
public class SimpleRunnerPool<T extends HostTestRunner> {
    private int limit;
    private int runnerNumber;
    private PriorityBlockingQueue<T> queue;
    private List<PutRunnerListener<T>> putListeners = new LinkedList<PutRunnerListener<T>>();

    public SimpleRunnerPool(int limit) {
        this.limit = limit;
        this.queue = new PriorityBlockingQueue<>(limit);
    }

    public boolean put(T runner) {
        queue.put(runner);

        for (PutRunnerListener<T> putListener : putListeners) {
            putListener.handle(runner);
        }

        if(runnerNumber < limit){
            runnerNumber++;
            return true;
        } else {
            return false;
        }
    }

    public T take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return limit;
    }

    public void addPutListener(PutRunnerListener<T> listener){
        this.putListeners.add(listener);
    }

    public void removePutListener(PutRunnerListener<T> listener){
        this.putListeners.remove(listener);
    }

    @FunctionalInterface
    public static interface PutRunnerListener<T extends HostTestRunner> {
        void handle(T runner);
    }
}
