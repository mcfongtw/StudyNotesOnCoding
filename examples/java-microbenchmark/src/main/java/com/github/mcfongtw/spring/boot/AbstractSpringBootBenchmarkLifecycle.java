package com.github.mcfongtw.spring.boot;

import com.github.mcfongtw.AbstractBenchmarkLifecycle;
import com.github.mcfongtw.BenchmarkLifecycle;
import com.github.mcfongtw.spring.SpringBootBenchmarkStater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class AbstractSpringBootBenchmarkLifecycle extends AbstractBenchmarkLifecycle  {

    private static final int DEFAULT_NUMBER_OF_ENTITIES = 1024;

    public static int numberOfEntities = 1;

    protected ConfigurableApplicationContext configurableApplicationContext;

    @Override
    public void preTrialSetUp() throws Exception {
        super.preTrialSetUp();
        try {
            String args = "";
            if(configurableApplicationContext == null) {
                configurableApplicationContext = SpringApplication.run(SpringBootBenchmarkStater.class, args );
            }

            numberOfEntities = Integer.valueOf(System.getProperty("numberOfEntities", Integer.toString(DEFAULT_NUMBER_OF_ENTITIES)));

            logger.info("Number Of Entities: [{}]", numberOfEntities);
        } catch(BeansException e) {
            logger.error("Failed to created application context: ", e.getMessage());
        }
    }


    @Override
    public void preTrialTearDown() throws Exception {
        super.preTrialTearDown();
        if (configurableApplicationContext != null) {
            configurableApplicationContext.close();
        }
    }

}
