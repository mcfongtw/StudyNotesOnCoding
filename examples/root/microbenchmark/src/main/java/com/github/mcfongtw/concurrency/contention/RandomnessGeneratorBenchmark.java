package com.github.mcfongtw.concurrency.contention;

import com.github.mcfongtw.BenchmarkBase;
import com.github.mcfongtw.SimpleBenchmarkLifecycle;
import lombok.Getter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
public class RandomnessGeneratorBenchmark extends BenchmarkBase {

    @Getter
    @State(Scope.Thread)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        @Param({"8", "32"})
        public int BIT_SIZE;

        //initialize seed
        public Random rand = new Random();

        public long seed;

        public AtomicLong atomicSeed;

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
            rand.setSeed(System.currentTimeMillis());
            seed = rand.nextLong();
            atomicSeed = new AtomicLong(seed);
            super.doIterationSetup();
        }

        @TearDown(Level.Iteration)
        @Override
        public void doIterationTearDown() throws Exception {
            super.doIterationTearDown();
        }
    }

    /////////////////

    @Benchmark
    @Threads(1)
    public int measureReentrantLock_1(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        Lock lock = state.getLock();
        lock.lock();
        try {
            long oldSeed = state.seed;
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            int remainder = (int) state.seed % state.BIT_SIZE;
            return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
        } finally {
            lock.unlock();
        }
    }

    @Benchmark
    @Threads(2)
    public int measureReentrantLock_2(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        Lock lock = state.getLock();
        lock.lock();
        try {
            long oldSeed = state.seed;
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            int remainder = (int) state.seed % state.BIT_SIZE;
            return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
        } finally {
            lock.unlock();
        }
    }

    @Benchmark
    @Threads(4)
    public int measureReentrantLock_4(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        Lock lock = state.getLock();
        lock.lock();
        try {
            long oldSeed = state.seed;
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            int remainder = (int) state.seed % state.BIT_SIZE;
            return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
        } finally {
            lock.unlock();
        }
    }

    /////////////////

    @Benchmark
    @Threads(1)
    public int measureSynchronized_1(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        synchronized (state.objectLock){
            long oldSeed = state.seed;
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            int remainder = (int) state.seed % state.BIT_SIZE;
            return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
        }
    }

    @Benchmark
    @Threads(2)
    public int measureSynchronized_2(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        synchronized (state.objectLock){
            long oldSeed = state.seed;
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            int remainder = (int) state.seed % state.BIT_SIZE;
            return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
        }
    }

    @Benchmark
    @Threads(4)
    public int measureSynchronized_4(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        synchronized (state.objectLock){
            long oldSeed = state.seed;
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            int remainder = (int) state.seed % state.BIT_SIZE;
            return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
        }
    }

    /////////////////

    @Benchmark
    @Threads(1)
    public int measureAtomicPrimitive_1(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        while(true) {
            long oldSeed = state.atomicSeed.get();
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            if(state.atomicSeed.compareAndSet(oldSeed, newSeed)) {
                int remainder = (int) state.seed % state.BIT_SIZE;
                return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
            }
        }
    }

    @Benchmark
    @Threads(2)
    public int measureAtomicPrimitive_2(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        while(true) {
            long oldSeed = state.atomicSeed.get();
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            if(state.atomicSeed.compareAndSet(oldSeed, newSeed)) {
                int remainder = (int) state.seed % state.BIT_SIZE;
                return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
            }
        }
    }

    @Benchmark
    @Threads(4)
    public int measureAtomicPrimitive_4(RandomnessGeneratorBenchmark.BenchmarkState state, Blackhole blackhole) {
        while(true) {
            long oldSeed = state.atomicSeed.get();
            long newSeed = computeNewSeed(oldSeed);
            state.seed = newSeed;

            if(state.atomicSeed.compareAndSet(oldSeed, newSeed)) {
                int remainder = (int) state.seed % state.BIT_SIZE;
                return remainder > 0 ? remainder : remainder + state.BIT_SIZE;
            }
        }
    }

    /////////////////

    //Java implementation for Random.nextInt()
    public static final long MULTIPLIER = 25214903917L;
    public static final int INCREMENT = 11;
    //2^48 - 1
    public static final long MODULUS = 281474976710655L;

    private long computeNewSeed(long currentSeed) {
        return (MULTIPLIER * currentSeed + INCREMENT) & MODULUS;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RandomnessGeneratorBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("RandomnessGeneratorBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}

