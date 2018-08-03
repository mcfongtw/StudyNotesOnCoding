package com.github.mcfongtw.metrics;

import com.codahale.metrics.*;

import java.util.concurrent.TimeUnit;

public class LatencyMetric implements Metered, Sampling,  Metric, Counting {
    private Timer latencyTimer;

    private Counter totalLatencyInMillis;

    private MetricRegistry registry = MetricUtils.getDefaultMetricRegistry();

    public LatencyMetric(String prefix) {
        latencyTimer = registry.timer(prefix + "Latency");
        totalLatencyInMillis = registry.counter(prefix + "TotalLatencyMillis");
    }

    public void addTime(long duration, TimeUnit unit) {
        latencyTimer.update(duration, unit);
        totalLatencyInMillis.inc(TimeUnit.MILLISECONDS.convert(duration, unit));
    }

    public long getTotalLatencyInMillis() {
        return totalLatencyInMillis.getCount();
    }

    public long getCount() {
        return latencyTimer.getCount();
    }

    public double getFifteenMinuteRate() {
        return latencyTimer.getFifteenMinuteRate();
    }


    public double getFiveMinuteRate() {
        return latencyTimer.getFiveMinuteRate();
    }

    public double getMeanRate() {
        return latencyTimer.getMeanRate();
    }

    public double getOneMinuteRate() {
        return latencyTimer.getOneMinuteRate();
    }

    public Snapshot getSnapshot() {
        return latencyTimer.getSnapshot();
    }
}
