package com.github.mcfongtw.metrics;

import com.codahale.metrics.Counter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MetricConcurrentTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final int MAX_ITERATION = 10000000;

    @Test
    public void testUnsafeIncCounter() {
        Counter counter = new Counter();
        AtomicBoolean isZeroSeen = new AtomicBoolean(false);

        /*
         * make sure writer thread has begun to increment / reset so that the reader thread could start to
         * read value
         */
        AtomicBoolean isReaderThreadReady = new AtomicBoolean(false);

        for(int i = 0; i < MAX_ITERATION; i ++) {
            int newVal = i + 1;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    MetricUtils.unsafeIncCounter(counter, newVal);
                    isReaderThreadReady.set(true);
                }
            });
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if(isReaderThreadReady.get() && counter.getCount() == 0) {
                        isZeroSeen.set(true);
                    }
                }
            });
        }
        Assertions.assertTrue(isZeroSeen.get());
    }

    @Test
    public void testSafeIncCounter() {
        Counter counter = new Counter();
        AtomicBoolean isZeroSeen = new AtomicBoolean(false);
        AtomicBoolean isStart = new AtomicBoolean(false);

        for(int i = 0; i < MAX_ITERATION; i ++) {
            int newVal = i + 1;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    MetricUtils.incCounter(counter, newVal);
                }
            });
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if(isStart.get() && counter.getCount() == 0) {
                        isZeroSeen.set(true);
                    }
                }
            });
        }
        Assertions.assertFalse(isZeroSeen.get());
    }

}
