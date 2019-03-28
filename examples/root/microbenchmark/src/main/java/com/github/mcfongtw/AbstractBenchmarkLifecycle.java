package com.github.mcfongtw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBenchmarkLifecycle implements BenchmarkLifecycle {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void preTrialSetUp() throws Exception {
        logger.trace("[preTrialSetUp]");
    }

    @Override
    public void doTrialSetUp() throws Exception {
        this.preTrialSetUp();
        logger.trace("[doTrialSetUp]");
        this.postTrialSetUp();
    }

    @Override
    public void postTrialSetUp() throws Exception {
        logger.trace("[postTrialSetUp]");
    }

    @Override
    public void preTrialTearDown() throws Exception {
        logger.trace("[preTrialTearDown]");
    }

    @Override
    public void doTrialTearDown() throws Exception {
        this.preTrialTearDown();
        logger.trace("[doTrialTearDown]");
        this.postTrialTearDown();
    }

    @Override
    public void postTrialTearDown() throws Exception {
        logger.trace("[postTrialTearDown]");
    }

    @Override
    public void preIterationSetup() throws Exception {
        logger.trace("[preIterationSetup]");
    }

    @Override
    public void doIterationSetup() throws Exception {
        this.preIterationSetup();
        logger.trace("[doIterationSetup]");
        this.postIterationSetup();
    }

    @Override
    public void postIterationSetup()  throws Exception {
        logger.trace("[postIterationSetup]");
    }

    @Override
    public void preIterationTearDown() throws Exception{
        logger.trace("[preIterationTearDown]");
    }

    @Override
    public void doIterationTearDown() throws Exception{
        this.preIterationTearDown();
        logger.trace("[doIterationTearDown]");
        this.postIterationTearDown();
    }

    @Override
    public void postIterationTearDown()  throws Exception {
        logger.trace("[postIterationTearDown]");
    }
}
