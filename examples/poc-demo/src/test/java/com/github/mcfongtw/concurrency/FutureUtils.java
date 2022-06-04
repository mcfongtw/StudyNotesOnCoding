package com.github.mcfongtw.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class FutureUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(FutureUtils.class);


    // avoid instantiation
    private FutureUtils() {

    }

    public static void cancelFutureAsync(final Future<?> future, final long delayDurationInMillis, boolean mayInterruptIfRunning) {
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
}
