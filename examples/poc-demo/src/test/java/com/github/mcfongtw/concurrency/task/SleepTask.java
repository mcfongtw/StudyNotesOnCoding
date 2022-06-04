package com.github.mcfongtw.concurrency.task;


public class SleepTask extends BechmarkTask<Void> {

    protected final long sleepTimeInMillis;

    public SleepTask(long sleepTime) {
        this.sleepTimeInMillis = sleepTime;
    }

    @Override
    protected Void doTask() throws InterruptedException {
        Thread.sleep(this.sleepTimeInMillis);

        return null;
    }
}
