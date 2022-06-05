package com.github.mcfongtw.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.annotations.VisibleForTesting;

public class MetricUtils {

    private MetricUtils() {
        //avoid instantiation
    }

    public static final String DEFAULT_METRIC_REGISTRY_NAME = "DEFAULT_REGISTRY";

    private static final MetricRegistry defaultMetricRegistry = SharedMetricRegistries.setDefault(DEFAULT_METRIC_REGISTRY_NAME);

    public static MetricRegistry getDefaultMetricRegistry() {
        return defaultMetricRegistry;
    }

    public static String concat(String prefix, String metricName) {
        return prefix + "." + metricName;
    }

    public static void incCounter(Counter counter, long newVal) {
        counter.inc(newVal - counter.getCount());
    }

    @VisibleForTesting
    /*
     * There is a chance that a reader thread could Counter.getCount() == 0;
     *
     * For details, check com.github.mcfongtw.metrics.MetricConcurrentTest#testUnsafeIncCounter
     */
    static void unsafeIncCounter(Counter counter, long newVal) {
        long oldVal = counter.getCount();
        counter.dec(oldVal);
        counter.inc(newVal);
    }
}
