package com.github.mcfongtw.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class LatencyMetricUnitTest {

    private MetricRegistry metricRegistry = MetricUtils.getDefaultMetricRegistry();

    private LatencyMetric metric;

    private static final String TEST_NAME = "Test";

    @BeforeEach
    public void setUp() {
        metric = new LatencyMetric(TEST_NAME);
    }

    @AfterEach
    public void tearDoown() {
        metricRegistry.remove(TEST_NAME + "TotalLatencyMillis");
        metricRegistry.remove(TEST_NAME + "Latency");
    }

    @Test
    public void testGetLatencyMetric() {
        Assertions.assertTrue(metricRegistry.getCounters(MetricFilter.ALL).keySet().contains(TEST_NAME + "TotalLatencyMillis"));
        Assertions.assertTrue(metricRegistry.getTimers(MetricFilter.ALL).keySet().contains(TEST_NAME + "Latency"));
    }

    @Test
    public void testUseLatencyMetric() throws Exception {
        //1 sec
        metric.addTime(1000, TimeUnit.MILLISECONDS);

        Assertions.assertEquals(1, metric.getCount());
        Assertions.assertEquals(1000, metric.getTotalLatencyInMillis());

        //1 millis
        metric.addTime(1000, TimeUnit.MICROSECONDS);

        Assertions.assertEquals(2, metric.getCount());
        Assertions.assertEquals(1001, metric.getTotalLatencyInMillis());
    }


}
