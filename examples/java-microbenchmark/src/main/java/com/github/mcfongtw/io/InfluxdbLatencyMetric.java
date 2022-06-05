package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
import com.google.common.collect.ImmutableMap;

import static com.kickstarter.dropwizard.metrics.MetricsUtils.influxName;

public class InfluxdbLatencyMetric extends LatencyMetric {

    public InfluxdbLatencyMetric(String prefix) {
        super(influxName(prefix, ImmutableMap.of()));
    }
}
