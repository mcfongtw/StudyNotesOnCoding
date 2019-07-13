package com.github.mcfongtw.concurrency;

import com.github.mcfongtw.BenchmarkBase;
import com.github.mcfongtw.SimpleBenchmarkLifecycle;
import com.github.mcfongtw.StreamBenchmark;
import lombok.Getter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class LockBenchmark extends BenchmarkBase {

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        public long value = 0;

        private AtomicLong atomicLong = new AtomicLong(0);

        private Object objectLock = new Object();

        private Lock lock = new ReentrantLock();

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
    }


    @Benchmark
    public void measureReentrantLock(LockBenchmark.BenchmarkState state, Blackhole blackhole) {
        Lock lock = state.getLock();
        lock.lock();
        try {
            state.value++;
        } finally {
            lock.unlock();
        }
    }

    @Benchmark
    public void measureSynchronized(LockBenchmark.BenchmarkState state, Blackhole blackhole) {
        synchronized (state.getObjectLock()) {
            state.value++;
        }
    }

    @Benchmark
    public void measureAtomicPrimitive(LockBenchmark.BenchmarkState state, Blackhole blackhole) {
        state.getAtomicLong().getAndAdd(1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LockBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("LockBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}

