package task.scheduler;

public interface WorkerListener {

    void taskFinished(WorkerThread worker);
}
