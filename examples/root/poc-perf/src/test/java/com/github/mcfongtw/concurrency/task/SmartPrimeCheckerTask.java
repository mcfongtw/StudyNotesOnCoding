package com.github.mcfongtw.concurrency.task;

public class SmartPrimeCheckerTask extends SimplePrimeCheckerTask {

    public SmartPrimeCheckerTask(long num) {
        super(num);
    }

    @Override
    protected Boolean doTask() throws Exception {
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
                logger.warn("Prime Checking process is interrupted!");
                return false;
            }
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
