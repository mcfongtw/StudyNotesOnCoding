package com.github.mcfongtw.concurrency.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class SimplePrimeCheckerTask extends BechmarkTask<Boolean> implements Callable<Boolean> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final long num;

    public SimplePrimeCheckerTask(long num) {
        this.num = num;
    }

    @Override
    public Boolean call() throws Exception {
        return this.measure();
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
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
