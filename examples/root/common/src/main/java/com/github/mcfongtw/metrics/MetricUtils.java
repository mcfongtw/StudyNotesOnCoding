package com.github.mcfongtw.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class MetricUtils {

    private MetricUtils() {
        //avoid instantiation
    }

    public static final String DEFAULT_METRIC_REGISTRY_NAME = "DEFAULT_REGISTRY";

    private static final MetricRegistry defaultMetricRegistry = SharedMetricRegistries.setDefault(DEFAULT_METRIC_REGISTRY_NAME);

    public static MetricRegistry getDefaultMetricRegistry() {
        return defaultMetricRegistry;
    }
}
