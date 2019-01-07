package com.github.mcfongtw.spring.boot;

import com.github.mcfongtw.ExecutableLifecycle;
import com.github.mcfongtw.spring.SpringBootBenchmarkStater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class AbstractSpringBootBenchmark {

    public static int NUMBER_OF_ENTITIES = 1024;

    public static class AbstractSpringBootExecutionPlan implements ExecutableLifecycle {
        protected ConfigurableApplicationContext configurableApplicationContext;

        protected Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void preTrialSetUp() throws Exception {
            logger.trace("[preTrialSetUp]");
            try {
                String args = "";
                if(configurableApplicationContext == null) {
                    configurableApplicationContext = SpringApplication.run(SpringBootBenchmarkStater.class, args );
                }
            } catch(BeansException e) {
                logger.error("Failed to created application context: ", e.getMessage());
            }
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
            if (configurableApplicationContext != null) {
                configurableApplicationContext.close();
            }
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
}
