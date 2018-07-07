package com.github.mcfongtw.concurrency;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static com.github.mcfongtw.concurrency.FutureTaskAsyncTest.LOGGER;

public class FutureTaskAsyncTest {

    final static Logger LOGGER = LoggerFactory.getLogger(FutureTaskAsyncTest.class);

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
        Future<Double> future = this.executorService.submit(task);
        Awaitility.await().between(testDurationInMilliSecond, TimeUnit.MILLISECONDS, testDurationInMilliSecond + 100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
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
        Future<Double> future = this.executorService.submit(task);

        cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

        Awaitility.await().between(cancellationDelayInMillisSecond, TimeUnit.MILLISECONDS, testDurationInMilliSecond + 100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
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
        Future<Double> future = this.executorService.submit(task);

        cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

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
        }
    }

    @Test
    public void testInterruptPrimeCheckerFuture() throws Exception {
        final long cancellationDelayInMillisSecond = 10;
        SimplePrimeCheckerTask task = new SimplePrimeCheckerTask(BIG_PRIME);
        Future<Boolean> future = this.executorService.submit(task);

        cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

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
        }
    }

    @Test
    public void testInterruptSmartPrimeCheckerFuture() throws Exception {
        final long cancellationDelayInMillisSecond = 10;
        SimplePrimeCheckerTask task = new SmartPrimeCheckerTask(BIG_PRIME);
        Future<Boolean> future = this.executorService.submit(task);

        cancelFutureAsync(future, cancellationDelayInMillisSecond, true);

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
        }
    }

    private static void cancelFutureAsync(final Future<?> future, final long delayDurationInMillis, boolean mayInterruptIfRunning) {
        Runnable cancellation = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delayDurationInMillis);
                    future.cancel(mayInterruptIfRunning);
                } catch (InterruptedException ie) {
                    LOGGER.error("Task interrupted due to {}", ie.getClass().getName());
                }
            }
        };

        new Thread(cancellation).start();
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        this.executorService.shutdown();
    }


}

class SleepTask implements Callable<Double> {
    protected final long sleepTimeInMillis;

    public SleepTask(long sleepTime) {
        this.sleepTimeInMillis = sleepTime;
    }

    @Override
    public Double call() throws Exception {
        double begin = System.nanoTime();

        Thread.sleep(this.sleepTimeInMillis);

        double end = System.nanoTime();
        double elapsedTime = (end - begin) / 1E6;

        LOGGER.info("elapsed Time : {} ms", elapsedTime);

        return elapsedTime;
    }
}

class SimplePrimeCheckerTask implements Callable<Boolean> {

    protected final long num;

    public SimplePrimeCheckerTask(long num) {
        this.num = num;
    }

    @Override
    public Boolean call() throws Exception {
        double begin = System.nanoTime();

        Boolean result = this.isPrime();

        double end = System.nanoTime();
        double elapsedTime = (end - begin) / 1E6;

        LOGGER.info("elapsed Time : {} ms", elapsedTime);

        return result;
    }

    protected boolean isPrime() {
        if (num < 2) {
            return false;
        }
        if (num == 2) {
            return true;
        }
        // even number
        if (num % 2 == 0) { return false;
        }

        // odd number
        for (int i = 3; i * i <= num; i += 2) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

}


class SmartPrimeCheckerTask extends SimplePrimeCheckerTask {

    public SmartPrimeCheckerTask(long num) {
        super(num);
    }

    @Override
    protected boolean isPrime() {
        if (num < 2) {
            return false;
        }
        if (num == 2) {
            return true;
        }
        // even number
        if (num % 2 == 0) { return false;
        }

        // odd number
        for (int i = 3; i * i <= num; i += 2) {
            if(Thread.currentThread().isInterrupted()) {
                LOGGER.warn("Prime Checking process is interrupted!");
                return false;
            }
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}