package edu.san.luc.hosts_monitoring.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by sanya on 28.11.15.
 */
public class SimpleRunnerPool<T extends HostTestRunner> {
    private int size;
    private PriorityBlockingQueue<T> queue;
    private List<PutRunnerListener<T>> putListeners = new LinkedList<PutRunnerListener<T>>();

    public SimpleRunnerPool(int size) {
        this.size = size;
        this.queue = new PriorityBlockingQueue<>(size);
    }

    public boolean put(T runner) {
        queue.put(runner);

        return queue.size() < size;
    }

    public T take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return size;
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
