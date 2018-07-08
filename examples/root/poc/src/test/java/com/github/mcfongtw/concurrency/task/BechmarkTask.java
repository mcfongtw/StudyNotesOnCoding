package com.github.mcfongtw.concurrency.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BechmarkTask<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected abstract T doTask() throws Exception;

    protected T measure() throws Exception {
        double begin = System.nanoTime();

        T taskResult = this.doTask();

        double end = System.nanoTime();
        double elapsedTime = (end - begin) / 1E6;

        logger.info("elapsed Time : {} ms", elapsedTime);

        return taskResult;
    }
}
