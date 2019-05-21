package task.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ScheduledTask implements Comparable<ScheduledTask> {
    private final long sequenceNumber;
    private final LocalDateTime executionTime;
    private final Callable callable;

    public ScheduledTask(Callable callable,
                         LocalDateTime executionTime,
                         long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.executionTime = executionTime;
        this.callable = callable;
    }


    public long getDelayNanos() {
        return getDelay();
    }

    public Callable getCallable() {
        return callable;
    }

    @Override
    public int compareTo(ScheduledTask other) {
        if (other == this)
            return 0;
        int compare = compareByExecutionTime(other);
        if (compare == 0) {
            return compareBySequenceNumber(other);
        }
        return compare;
    }

    public boolean isReady() {
        return getDelay() <= 0;
    }

    private int compareBySequenceNumber(ScheduledTask other) {
        if (sequenceNumber < other.sequenceNumber)
            return -1;
        return 1;
    }

    private int compareByExecutionTime(ScheduledTask other) {
        long diff = getDelay() - other.getDelay();
        if (diff < 0) return -1;
        if (diff > 0) return 1;
        return 0;
    }

    private long nanosExecutionTime() {
        Instant instant = executionTime.toInstant(OffsetDateTime.now().getOffset());
        return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    }

    private long getDelay() {
        return nanosExecutionTime() - System.nanoTime();
    }
}
