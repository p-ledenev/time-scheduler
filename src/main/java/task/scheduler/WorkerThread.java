package task.scheduler;

import java.util.concurrent.Callable;

public class WorkerThread implements Runnable {
    private final Thread thread;
    private final WorkerListener workerListener;
    private Callable callable;

    public WorkerThread(WorkerListener workerListener) {
        this.workerListener = workerListener;
        thread = new Thread(this);
    }

    public void executeTask(Callable callable) {
        this.callable = callable;
        thread.run();
    }

    @Override
    public void run() {
        try {
            callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            workerListener.taskFinished(this);
        }
    }
}
