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
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class CommonHashBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        public Hasher murmur3;

        public Hasher sha256;

        public String password = "1gq3t123fasdsg1334";

        @Setup(Level.Trial)
        @Override
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
        }

        @TearDown(Level.Trial)
        @Override
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();
        }

        @Setup(Level.Iteration)
        @Override
        public void doIterationSetup() throws Exception {
            super.doIterationSetup();
        }

        @TearDown(Level.Iteration)
        @Override
        public void doIterationTearDown() throws Exception {
            super.doIterationTearDown();
        }

        @Setup(Level.Invocation)
        @Override
        public void doInvocationSetup() throws Exception {
            super.doInvocationSetup();
            murmur3 = Hashing.murmur3_128().newHasher();
            sha256 = Hashing.sha256().newHasher();
        }

        @TearDown(Level.Invocation)
        @Override
        public void doInvocationTearDown() throws Exception{
            super.doInvocationTearDown();
            murmur3 = null;
            sha256 = null;
        }
    }

    @Benchmark
    public void measureMurmur3_128(BenchmarkState state) {
        state.murmur3.putString(state.password, Charset.defaultCharset());
        state.murmur3.hash();
    }

    @Benchmark
    public void measureSha256(BenchmarkState state) {
        state.sha256.putString(state.password, Charset.defaultCharset());
        state.sha256.hash();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CommonHashBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("CommonHashBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
