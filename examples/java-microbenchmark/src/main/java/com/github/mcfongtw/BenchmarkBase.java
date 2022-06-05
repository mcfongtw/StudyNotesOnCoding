package com.github.mcfongtw;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public abstract class BenchmarkBase {

    private static final int BASELINE_TOKEN = -1;

    @Benchmark
    public void measureBaseline() {
        Blackhole.consumeCPU(BASELINE_TOKEN);
    }

}
