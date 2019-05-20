package task.scheduler.testing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import task.scheduler.TasksScheduler;

public class ManagerTesting {
    private TasksScheduler tasksScheduler;

    @Before
    public void setup() {
        tasksScheduler = new TasksScheduler(1);
        tasksScheduler.start();
    }

    @After
    public void tearDown() {
        tasksScheduler.stop();
    }

    @Test
    public void shouldRunTask() {
       tasksScheduler.add(Callable)
    }
}
