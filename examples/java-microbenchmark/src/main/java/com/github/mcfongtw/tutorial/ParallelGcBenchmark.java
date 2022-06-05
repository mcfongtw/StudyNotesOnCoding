package com.github.mcfongtw.tutorial;

import com.github.mcfongtw.ObjectReferenceBenchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(value = 3, jvmArgsAppend = {"-XX:+PrintGCDetails", "-XX:+UseParallelGC"})
@Threads(1)
public class ParallelGcBenchmark extends ObjectReferenceBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParallelGcBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("ParallelGcBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}


