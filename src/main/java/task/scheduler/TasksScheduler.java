package task.scheduler;

import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.isNull;

public class TasksScheduler implements WorkerListener {
    private final Thread managingThread;
    private final PriorityQueue<ScheduledTask> tasksQueue;
    private final WorkersSet workersSet;
    private final ReentrantLock monitor;
    private final Condition stateChanged;
    private final AtomicLong sequencer;

    public TasksScheduler(int workersNumber) {
        managingThread = new Thread(this::runManager);
        tasksQueue = new PriorityQueue<>();
        monitor = new ReentrantLock();
        stateChanged = monitor.newCondition();
        sequencer = new AtomicLong();
        workersSet = new WorkersSet(workersNumber, this);
    }

    public void start() {
        managingThread.start();
    }

    public void stop() {
        managingThread.interrupt();
    }

    public void addTask(Callable callable, LocalDateTime executionTime) {
        monitor.lock();
        try {
            tasksQueue.add(createTask(callable, executionTime));
            stateChanged.signalAll();
        } finally {
            monitor.unlock();
        }
    }

    @Override
    public void taskFinished(WorkerThread worker) {
        monitor.lock();
        try {
            workersSet.markFree(worker);
            stateChanged.signalAll();
        } finally {
            monitor.unlock();
        }
    }

    private void runManager() {
        monitor.lock();
        System.out.println("Scheduler manager started");
        try {
            for (; ; ) {
                ScheduledTask nextTask = tasksQueue.peek();
                if (isNull(nextTask)) {
                    stateChanged.await();
                } else if (nextTask.isReady() && workersSet.hasFree()) {
                    tasksQueue.poll();
                    workersSet.executeTask(nextTask.getCallable());
                } else {
                    stateChanged.awaitNanos(nextTask.getDelayNanos());
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Manager was shut down");
        } finally {
            monitor.unlock();
        }
    }

    private ScheduledTask createTask(Callable callable, LocalDateTime executionTime) {
        return new ScheduledTask(callable, executionTime, sequencer.getAndIncrement());
    }
}
