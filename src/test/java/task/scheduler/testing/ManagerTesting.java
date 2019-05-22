package task.scheduler.testing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import task.scheduler.TasksScheduler;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.LocalDateTime.now;

public class ManagerTesting {
    private final static int MAX_WAIT_ATTEMPTS = 5;
    private final static TasksScheduler TASKS_SCHEDULER = new TasksScheduler(2);

    private AtomicLong firstTaskExecutedMillis = new AtomicLong();
    private AtomicLong secondTaskExecutedMillis = new AtomicLong();

    @BeforeClass
    public static void setup() {
        TASKS_SCHEDULER.start();
    }

    @AfterClass
    public static void tearDown() {
        TASKS_SCHEDULER.stop();
    }

    @Test
    public void shouldSchedulerTaskProperly() throws Exception {
        long nowMillis = System.currentTimeMillis();

        scheduleTestTask(firstTaskExecutedMillis, now().plusSeconds(1L));
        waitForTaskToBeProcessed(1_100, firstTaskExecutedMillis);

        assert firstTaskExecutedMillis.get() - nowMillis >= 1_000;
    }

    @Test
    public void shouldProcessWhenTaskThrowsException() throws Exception {
        scheduleFallingTask(now().plusSeconds(1L));
        scheduleTestTask(secondTaskExecutedMillis, now().plusSeconds(2L));

        waitForTaskToBeProcessed(2_200, secondTaskExecutedMillis);

        assert secondTaskExecutedMillis.get() > 0;
    }

    @Test
    public void shouldRunTaskInProperOrderForDifferentExecutionTime() throws Exception {
        scheduleTestTask(secondTaskExecutedMillis, now().plusSeconds(2L));
        scheduleTestTask(firstTaskExecutedMillis, now().plusSeconds(1L));

        waitForTasksToBeProcessed(1_000);

        assertExecutionFirstBeforeSecondWithDelay(900L);
    }

    @Test
    public void shouldRunTaskInProperOrderForSameExecutionTime() throws Exception {
        scheduleTestTask(firstTaskExecutedMillis, now().plusSeconds(1L));
        scheduleTestTask(secondTaskExecutedMillis, now().plusSeconds(1L));

        waitForTasksToBeProcessed(1_000);

        assertExecutionFirstBeforeSecondWithDelay(0L);
    }

    private void assertExecutionFirstBeforeSecondWithDelay(long delayMillis) {
        assert firstTaskExecutedMillis.get() > 0;
        assert secondTaskExecutedMillis.get() > 0;
        assert secondTaskExecutedMillis.get() - firstTaskExecutedMillis.get() >= delayMillis;
    }

    private void scheduleTestTask(AtomicLong executedNanos, LocalDateTime executionTime) {
        TASKS_SCHEDULER.addTask(() -> testTask(executedNanos), executionTime);
    }

    private void scheduleFallingTask(LocalDateTime localDateTime) {
        TASKS_SCHEDULER.addTask(
                () -> {
                    throw new RuntimeException("Some error occurred");
                },
                localDateTime
        );
    }

    private Object testTask(AtomicLong executedNanos) {
        executedNanos.set(System.currentTimeMillis());
        return null;
    }

    private void waitForTasksToBeProcessed(long waitTimeMillis) throws Exception {
        waitForTaskToBeProcessed(waitTimeMillis, firstTaskExecutedMillis);
        waitForTaskToBeProcessed(waitTimeMillis, secondTaskExecutedMillis);
    }

    private void waitForTaskToBeProcessed(long waitTimeMillis, AtomicLong executedMillis) throws Exception {
        for (int i = 0; i < MAX_WAIT_ATTEMPTS; i++) {
            Thread.sleep(waitTimeMillis);
            if (executedMillis.get() > 0)
                return;
        }
    }
}
