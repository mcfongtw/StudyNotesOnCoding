package com.github.mcfongtw.io;

public interface ExecutableLifecycle {

    void preTrialSetUp() throws Exception;

    void doTrialSetUp() throws Exception;

    void postTrialSetUp() throws Exception;

    void preTrialTearDown() throws Exception;

    void doTrialTearDown() throws Exception;

    void postTrialTearDown() throws Exception;

    void preIterationSetup() throws Exception;

    void doIterationSetup() throws Exception;

    void postIterationSetup()  throws Exception;

    void preIterationTearDown() throws Exception;

    void doIterationTearDown() throws Exception;

    void postIterationTearDown()  throws Exception;
}
