package task.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.isNull;

public class WorkerThread implements Runnable {
    private final Thread thread;
    private final WorkerListener workerListener;
    private final Lock monitor;
    private final Condition hasTask;
    private Callable callable;

    public WorkerThread(WorkerListener workerListener) {
        this.workerListener = workerListener;
        thread = new Thread(this);
        monitor = new ReentrantLock();
        hasTask = monitor.newCondition();
        thread.start();
    }

    public void executeTask(Callable callable) {
        monitor.lock();
        try {
            this.callable = callable;
            hasTask.signalAll();
        } finally {
            monitor.unlock();
        }
    }

    @Override
    public void run() {
        monitor.lock();
        try {
            for (; ; ) {
                if (isNull(callable)) {
                    hasTask.await();
                } else {
                    executeCallable();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Running thread stopped");
        } finally {
            monitor.unlock();
        }
    }

    private void executeCallable() {
        try {
            System.out.println("Running task in thread " + Thread.currentThread().getName());
            callable.call();
        } catch (Exception e) {
            System.out.println("Error occurred during executing task");
            e.printStackTrace();
        } finally {
            callable = null;
            workerListener.taskFinished(this);
        }
    }
}
