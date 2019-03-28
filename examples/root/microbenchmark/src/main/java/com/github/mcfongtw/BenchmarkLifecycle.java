package com.github.mcfongtw;

public interface BenchmarkLifecycle {

    ////////////[Setup Trial]////////////////////////

    void preTrialSetUp() throws Exception;

    void doTrialSetUp() throws Exception;

    void postTrialSetUp() throws Exception;

    ////////////[TearDown Trial]////////////////////////

    void preTrialTearDown() throws Exception;

    void doTrialTearDown() throws Exception;

    void postTrialTearDown() throws Exception;

    ////////////[Setup Iteration]////////////////////////

    void preIterationSetup() throws Exception;

    void doIterationSetup() throws Exception;

    void postIterationSetup()  throws Exception;

    ////////////[TearDown Iteration]////////////////////////
    // WARNING: Level.Invocation should be used carefully!
    //////////////////////////////////////////////////////

    void preIterationTearDown() throws Exception;

    void doIterationTearDown() throws Exception;

    void postIterationTearDown()  throws Exception;


    ////////////[Setup Invocation]////////////////////////
    // WARNING: Level.Invocation should be used carefully!
    //
    // Level.Invocation influences the WarmUp iterations
    //////////////////////////////////////////////////////

    void preInvocationSetup() throws Exception;

    void doInvocationSetup() throws Exception;

    void postInvocationSetup()  throws Exception;

    ////////////[TearDown Invocation]////////////////////////

    void preInvocationTearDown() throws Exception;

    void doInvocationTearDown() throws Exception;

    void postInvocationTearDown()  throws Exception;
}
