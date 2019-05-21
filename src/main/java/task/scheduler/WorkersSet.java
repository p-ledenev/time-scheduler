package task.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class WorkersSet {
    private final List<WorkerThread> freeWorkers;
    private final List<WorkerThread> busyWorkers;

    public WorkersSet(int workersNumber, WorkerListener workerListener) {
        freeWorkers = IntStream.range(0, workersNumber)
                .boxed()
                .map(i -> new WorkerThread(workerListener))
                .collect(toList());
        busyWorkers = new ArrayList<>();
    }

    public boolean hasFree() {
        return !freeWorkers.isEmpty();
    }

    public void markFree(WorkerThread worker) {
        busyWorkers.remove(worker);
        freeWorkers.add(worker);
    }

    public void executeTask(Callable callable) {
        allocateFree().executeTask(callable);
    }

    private WorkerThread allocateFree() {
        WorkerThread worker = freeWorkers.remove(freeWorkers.size() - 1);
        busyWorkers.add(worker);
        return worker;
    }
}
