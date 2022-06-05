package com.github.mcfongtw.io;

import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.mcfongtw.metrics.MetricUtils;
import com.kickstarter.dropwizard.metrics.influxdb.InfluxDbMeasurementReporterFactory;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InfluxdbReporterSingleton {

    private static Logger logger = LoggerFactory.getLogger(InfluxdbReporterSingleton.class);

    private static ScheduledReporter INSTANCE = null;

    private InfluxdbReporterSingleton() {

    }

    public static ScheduledReporter newInstance() {
        if(INSTANCE == null) {
            INSTANCE = initInfluxdbReporter();
        }

        return INSTANCE;
    }

    private static ScheduledReporter initInfluxdbReporter() {
        ScheduledReporter scheduledReporter = null;

        final ObjectMapper mapper = Jackson.newObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        final InfluxDbMeasurementReporterFactory factory;

        try {
            factory = mapper.readValue(InfluxdbReporterSingleton.class.getResourceAsStream("/influxdb.json"), InfluxDbMeasurementReporterFactory.class);
            scheduledReporter = factory.build(MetricUtils.getDefaultMetricRegistry());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            return scheduledReporter;
        }
    }
}
