package com.github.mcfongtw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBenchmarkLifecycle implements BenchmarkLifecycle {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    ////////////[Setup Trial]////////////////////////

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

    ////////////[TearDown Trial]////////////////////////

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

    ////////////[Setup Iteration]////////////////////////

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

    ////////////[TearDown Iteration]////////////////////////

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


    ////////////[Setup Invocation]////////////////////////
    // WARNING: Level.Invocation should be used carefully!
    //
    // Level.Invocation influences the WarmUp iterations
    //////////////////////////////////////////////////////

    @Override
    public void preInvocationSetup() throws Exception {
        logger.trace("[preInvocationSetup]");
    }

    @Override
    public void doInvocationSetup() throws Exception {
        this.preInvocationSetup();
        logger.trace("[doInvocationSetup]");
        this.postInvocationSetup();
    }

    @Override
    public void postInvocationSetup()  throws Exception {
        logger.trace("[postInvocationSetup]");
    }

    ////////////[TearDown Invocation]////////////////////////
    // WARNING: Level.Invocation should be used carefully!
    //////////////////////////////////////////////////////

    @Override
    public void preInvocationTearDown() throws Exception{
        logger.trace("[preInvocationTearDown]");
    }

    @Override
    public void doInvocationTearDown() throws Exception{
        this.preInvocationTearDown();
        logger.trace("[doInvocationTearDown]");
        this.postInvocationTearDown();
    }

    @Override
    public void postInvocationTearDown()  throws Exception {
        logger.trace("[postIterationTearDown]");
    }
}
