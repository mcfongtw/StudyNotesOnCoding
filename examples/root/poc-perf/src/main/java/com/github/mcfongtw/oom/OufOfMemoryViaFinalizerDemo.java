package com.github.mcfongtw.oom;

import java.util.concurrent.atomic.AtomicInteger;

public class OufOfMemoryViaFinalizerDemo {

    static AtomicInteger aliveCount = new AtomicInteger(0);

    OufOfMemoryViaFinalizerDemo() {
        aliveCount.incrementAndGet();
    }

    @Override
    protected void finalize() throws Throwable {
        OufOfMemoryViaFinalizerDemo.aliveCount.decrementAndGet();
    }

    public static void main(String args[]) {
        for (int i = 0;; i++) {
            OufOfMemoryViaFinalizerDemo instance = new OufOfMemoryViaFinalizerDemo();
            if ((i % 100_000) == 0) {
                System.out.format("After creating %d objects, %d are still alive.%n", new Object[] {i, OufOfMemoryViaFinalizerDemo.aliveCount.get() });
            }
        }
    }
}
