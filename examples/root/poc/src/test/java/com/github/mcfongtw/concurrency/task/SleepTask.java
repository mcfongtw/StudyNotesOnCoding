package com.github.mcfongtw.concurrency.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class SleepTask extends BechmarkTask<Void> implements Callable<Void>{

    protected final long sleepTimeInMillis;

    public SleepTask(long sleepTime) {
        this.sleepTimeInMillis = sleepTime;
    }

    @Override
    public Void call() throws Exception {
        return this.measure();
    }

    @Override
    protected Void doTask() throws InterruptedException {
        Thread.sleep(this.sleepTimeInMillis);

        return null;
    }
}
