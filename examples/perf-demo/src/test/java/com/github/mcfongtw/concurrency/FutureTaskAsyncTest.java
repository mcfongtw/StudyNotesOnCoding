package com.github.mcfongtw.concurrency;

import com.github.mcfongtw.concurrency.task.SimplePrimeCheckerTask;
import com.github.mcfongtw.concurrency.task.SleepTask;
import com.github.mcfongtw.concurrency.task.SmartPrimeCheckerTask;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;


public class FutureTaskAsyncTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(FutureTaskAsyncTest.class);

    private static final long AWAIT_BETWEEN_DELAY_BUFFER_IN_MILLIS = 500;

    private static final long BIG_PRIME = 1000000000000003L;

    private ExecutorService executorService;

    @BeforeEach
    public void beforeEachTest() throws Exception {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testSleepFuture() throws Exception {
        final long testDurationInMilliSecond = 3_000;
        SleepTask task = new SleepTask(testDurationInMilliSecond);
        Future<Void> future = this.executorService.submit(task, null);
        Awaitility.await().between(testDurationInMilliSecond, TimeUnit.MILLISECONDS, testDurationInMilliSecond + AWAIT_BETWEEN_DELAY_BUFFER_IN_MILLIS, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return future.isDone();
            }
        });
        future.get();
    }

    @Test
    public void testCancelRunningSleepFuture() throws Exception {
        final long testDurationInMilliSecond = 3_000;
        final long cancellationDelayInMillisSecond = 100;
        SleepTask task = new SleepTask(testDurationInMilliSecond);
        Future<Void> future = this.executorService.submit(task, null);

        FutureUtils.cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, testDurationInMilliSecond + AWAIT_BETWEEN_DELAY_BUFFER_IN_MILLIS, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
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
        }
    }

    @Test
    public void testInterruptSleepFuture() throws Exception {
        final long testDurationInMilliSecond = 3_000;
        final long cancellationDelayInMillisSecond = 100;
        SleepTask task = new SleepTask(testDurationInMilliSecond);
        Future<Void> future = this.executorService.submit(task, null);

        FutureUtils.cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, cancellationDelayInMillisSecond + AWAIT_BETWEEN_DELAY_BUFFER_IN_MILLIS, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
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
        }
    }

    @Test
    public void testInterruptPrimeCheckerFuture() throws Exception {
        final long cancellationDelayInMillisSecond = 10;
        SimplePrimeCheckerTask task = new SimplePrimeCheckerTask(BIG_PRIME);
        Future<Boolean> future = this.executorService.submit(task, Boolean.FALSE);

        FutureUtils.cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, cancellationDelayInMillisSecond + AWAIT_BETWEEN_DELAY_BUFFER_IN_MILLIS, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
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
        }
    }

    @Test
    public void testInterruptSmartPrimeCheckerFuture() throws Exception {
        final long cancellationDelayInMillisSecond = 10;
        SimplePrimeCheckerTask task = new SmartPrimeCheckerTask(BIG_PRIME);
        Future<Boolean> future = this.executorService.submit(task, Boolean.FALSE);

        FutureUtils.cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, cancellationDelayInMillisSecond + AWAIT_BETWEEN_DELAY_BUFFER_IN_MILLIS, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
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
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        this.executorService.shutdown();
    }


}
