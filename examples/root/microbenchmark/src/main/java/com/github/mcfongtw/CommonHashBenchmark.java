package com.github.mcfongtw;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.Charset;

public class CommonHashBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        public Hasher murmur3;

        public Hasher sha256;

        public String password = "1gq3t123fasdsg1334";

        @Setup(Level.Invocation)
        public void doSetUp() {
            murmur3 = Hashing.murmur3_128().newHasher();
            sha256 = Hashing.sha256().newHasher();
        }

        @TearDown(Level.Invocation)
        public void doTearDown() {
            murmur3 = null;
            sha256 = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Measurement(iterations=100)
    public void measureMurmur3_128(ExecutionPlan plan) {
        plan.murmur3.putString(plan.password, Charset.defaultCharset());
        plan.murmur3.hash();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @Measurement(iterations=100)
    public void measureSha256(ExecutionPlan plan) {
        plan.sha256.putString(plan.password, Charset.defaultCharset());
        plan.sha256.hash();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CommonHashBenchmark.class.getSimpleName())
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("CommonHashBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
