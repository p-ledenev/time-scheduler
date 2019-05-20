package task.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

public class TasksScheduler {
    private final Thread managingThread;
    private final TasksPriorityQueue tasksQueue;
    private final WorkerThreads workerThreads;
    private final ReentrantLock monitor;

    public TasksScheduler(int workersNumber) {
        managingThread = new Thread();
        tasksQueue = new TasksPriorityQueue();
        monitor = new ReentrantLock();
        workerThreads = new WorkerThreads(workersNumber);
    }

    public void start() {

    }

    public void stop() {

    }

    public <V> void addTask(Callable<V> callable) {
        monitor.lock();
        try {
            tasksQueue.add(callable);
            monitor.notifyAll();
        }
        finally {
            monitor.unlock();
        }
    }

    private void run() {
        monitor.lock();
        try {
            for(;;) {
                TimedTask nextTask = tasksQueue.get();
                if (nextTask.getDelay() < 0 && workerThreads.hasFree()) {
                    tasksQueue.pull();
                    workerThreads.run(nextTask.getCallable());
                } else {
                    monitor.wait(nextTask.getDelay());
                }
            }
        }
    }
}
