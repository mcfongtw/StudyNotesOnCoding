package com.github.mcfongtw.concurrency.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

public abstract class BechmarkTask<T> implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected LongAdder counter = new LongAdder();

    private void preTask() {
        logger.info("[{}] Started", this.getClass().getSimpleName());
    }

    private void postTask() {
        counter.increment();
        logger.info("[{}] Stopped", this.getClass().getSimpleName());
    }

    protected abstract T doTask() throws Exception;

    @Override
    public void run(){
        try {
            this.doTaskWithBenchmark();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    protected T doTaskWithBenchmark() throws Exception {
        double begin = System.nanoTime();
        this.preTask();

        T taskResult = this.doTask();

        if ( taskResult != null) {
            logger.info("[{}] taskResult = [{}]", this.getClass().getSimpleName(), taskResult);
        }


        this.postTask();

        double end = System.nanoTime();
        double elapsedTime = (end - begin) / 1E6;

        logger.info("[{}] elapsed Time : {} ms", this.getClass().getSimpleName(), elapsedTime);

        return taskResult;
    }

    public long getCount()  {
        return this.counter.longValue();
    }
}
