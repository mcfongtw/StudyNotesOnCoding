package com.github.mcfongtw.concurrency;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/*
 * 1. CountDownLatch
 * 2. CyclicBarrier
 * 3. Semaphore
 *
 */
public class CommonJavaConcurrentTest {
    /*
     * A CountDownLatch is a construct that a thread waits on while other threads
     * count down on the latch until it reaches zero.
     *
     * We can think of this like a dish at a restaurant that is being prepared. No
     * matter which cook prepares however many of the n items, the waiter must wait
     * until all the items are on the plate. If a plate takes n items, any cook will
     * count down on the latch for each item she puts on the plate.
     *
     * In short, CountDownLatch maintains a count of tasks.
     */

    @Test
    public void testSimpleCountDownLatch() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        //Starting one thread, calling countDown() events twice
        Thread t = new Thread(() -> {
            countDownLatch.countDown();
            countDownLatch.countDown();
        });
        t.start();
        countDownLatch.await();

        Assertions.assertEquals(0, countDownLatch.getCount());
    }

    /*
     * Even though 20 different threads call countDown(), the count doesn’t reset once it reaches zero.
     * CountDownLatch is different because the count never resets.
     */
    @Test
    public void testReusableCountDownLatch() {
        List<String> list = Lists.newArrayList();
        CountDownLatch countDownLatch = new CountDownLatch(7);
        ExecutorService es = Executors.newFixedThreadPool(20);

        for (int i = 0; i < 20; i++) {
            es.execute(() -> {
                long prevValue = countDownLatch.getCount();
                countDownLatch.countDown();
                if (countDownLatch.getCount() != prevValue) {
                    list.add("Count Updated");
                }
            });
        }
        es.shutdown();

        Assertions.assertTrue(list.size() <= 7);
    }


    /*
     * A CyclicBarrier is a reusable construct where a group of threads waits
     * together until all of the threads arrive. At that point, the barrier is
     * broken and an action can optionally be taken.
     *
     * We can think of this like a group of friends. Every time they plan to eat
     * at a restaurant they decide a common point where they can meet. They wait
     * for each other there, and only when everyone arrives can they go to the
     * restaurant to eat together.
     *
     * In short, CyclicBarrier maintains a count of threads
     */
    @Test
    public void testSimpleCyclicBarrier() throws BrokenBarrierException, InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        //Starting two threads, each calling await()
        IntStream.range(0, 2).forEach(action -> {
            Thread t = new Thread(() -> {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.out.println(e);
                }
            });
            t.start();
        });
        Thread.sleep(100);

        Assertions.assertEquals(0, cyclicBarrier.getNumberWaiting());
        Assertions.assertFalse(cyclicBarrier.isBroken());
    }

    /*
     * The value decreases every time a new thread runs, by resetting to the original value, once it
     * reaches zero. When the barrier trips in CyclicBarrier, the count resets to its original value.
     */
    @Test
    public void testReusableCyclicBarrier() throws InterruptedException {
        List<String> list = Lists.newArrayList();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7);

        ExecutorService es = Executors.newFixedThreadPool(20);
        IntStream.range(0, 20).forEach(action -> es.execute(() -> {
            try {
                if (cyclicBarrier.getNumberWaiting() == 0) {
                    //7 keeps decreasing to 0
                    list.add("Count Updated");
                }
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                // error handling
            }
        }));
        es.shutdown();

        // 20 / 7 ~ 3
        Assertions.assertTrue(list.size() >= 3);
    }


    @Test
    public void testSimpleSemaphore() throws InterruptedException {
        Semaphore semaphore = new Semaphore(2);
        ExecutorService acquireService = Executors.newFixedThreadPool(2);

        //[0, 2}
        IntStream.range(0, 2).forEach(action -> acquireService.submit(() -> {
            semaphore.tryAcquire();
        }));
        Thread.sleep(100);

        acquireService.shutdown();

        Assertions.assertEquals(0, semaphore.availablePermits());
        Assertions.assertFalse(semaphore.tryAcquire());

        semaphore.release();

        Assertions.assertEquals(1, semaphore.availablePermits());
        Assertions.assertTrue(semaphore.tryAcquire());
    }
}
