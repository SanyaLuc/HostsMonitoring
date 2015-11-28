package edu.san.luc.hosts_monitoring.runner;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by sanya on 28.11.15.
 */
public class SimpleRunnerPool<T> {
    private int size;
    private PriorityBlockingQueue<T> queue;

    public SimpleRunnerPool(int size) {
        this.size = size;
        this.queue = new PriorityBlockingQueue<>(size);
    }

    public boolean put(T runner){
        queue.put(runner);

        return queue.size() > size;
    }

    public T take() throws InterruptedException {
        return queue.take();
    }

    public int size(){
        return size;
    }
}
