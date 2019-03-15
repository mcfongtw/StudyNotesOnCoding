package com.github.mcfongtw;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class SystemTimeBenchmark {

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 20, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void measureCurrentTimeMillis() {
        System.currentTimeMillis();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 20, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void measureNanoTime() {
        System.nanoTime();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SystemTimeBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("SystemTimeBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
