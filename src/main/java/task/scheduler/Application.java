package task.scheduler;

public class Application {

    public static void main(String[] args) {
        TasksScheduler tasksScheduler = createManager();
        tasksScheduler.start();
    }

    private static TasksScheduler createManager() {
        return new TasksScheduler(1);
    }
}
