package com.github.mcfongtw.concurrency;

import com.github.mcfongtw.concurrency.task.SleepTask;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ScheduledExecutorAsyncTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduledExecutorAsyncTest.class);

    private ScheduledExecutorService scheduledExecutorService;

    @BeforeEach
    public void beforeEachTest() throws Exception {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Test
    public void testScheduleOneShotSleepTask() throws Exception {
        final long initialDelayInMilliSecond = 100;
        final long testDurationInMilliSecond = 3_000;
        SleepTask task = new SleepTask(testDurationInMilliSecond);
        ScheduledFuture future = this.scheduledExecutorService.schedule(task, initialDelayInMilliSecond, TimeUnit.MILLISECONDS);
        Awaitility.await().between(testDurationInMilliSecond + initialDelayInMilliSecond, TimeUnit.MILLISECONDS, testDurationInMilliSecond + initialDelayInMilliSecond + 100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return future.isDone();
            }
        });
        future.get();
    }

    @Test
    public void testCancelScheduleSleepTaskAtFixedRate() throws Exception {
        final long initialDelayInMilliSecond = 100;
        final long testDurationInMilliSecond = 3_000;
        final long cancellationDelayInMillisSecond = 10_000;
        SleepTask task = new SleepTask(testDurationInMilliSecond);
        ScheduledFuture future = this.scheduledExecutorService.scheduleAtFixedRate(task, initialDelayInMilliSecond, testDurationInMilliSecond, TimeUnit.MILLISECONDS);

        FutureUtils.cancelFutureAsync(future, cancellationDelayInMillisSecond, false);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, cancellationDelayInMillisSecond + 100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return future.isCancelled();
            }
        });

        try {
            future.get();
            Assertions.fail();
        } catch(CancellationException expected) {
            LOGGER.error("Task cancelled due to {}", expected.getClass().getName());
        } finally {
            Assertions.assertEquals(3, task.getCount());
        }
    }

    @Test
    public void testCancelScheduledSleepTaskWithFixedDelay() throws Exception {
        final long initialDelayInMilliSecond = 100;
        final long testDurationInMilliSecond = 3_000;
        final long cancellationDelayInMillisSecond = 10_000;
        SleepTask task = new SleepTask(testDurationInMilliSecond);
        ScheduledFuture future = this.scheduledExecutorService.scheduleWithFixedDelay(task, initialDelayInMilliSecond, testDurationInMilliSecond, TimeUnit.MILLISECONDS);

        FutureUtils.cancelFutureAsync(future, cancellationDelayInMillisSecond, false);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, cancellationDelayInMillisSecond + 100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return future.isCancelled();
            }
        });

        try {
            future.get();
            Assertions.fail();
        } catch(CancellationException expected) {
            LOGGER.error("Task cancelled due to {}", expected.getClass().getName());
        } finally {
            Assertions.assertEquals(2, task.getCount());
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        this.scheduledExecutorService.shutdown();
    }
}



