package com.github.mcfongtw;

import com.google.common.primitives.Ints;
import lombok.Getter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(value = 3)
public class StreamBenchmark extends BenchmarkBase {
    private static final int SIZE = 50_000_000;

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {
        private volatile int[] integers = new int[SIZE];

        @Setup(Level.Trial)
        @Override
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
            for ( int i=0; i < SIZE ; i++){
                if ( i < SIZE / 2){
                    integers[i] = i;
                }else {
                    integers[i] = i-SIZE;
                }
            }
        }

        @TearDown(Level.Trial)
        @Override
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();
            integers = null;
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
    public void measureSumViaWhileIterator(BenchmarkState state, Blackhole blackhole){
        int result = 0;
        Iterator<Integer> iterator = Ints.asList(state.getIntegers()).iterator();
        while ( iterator.hasNext()){
            result += iterator.next();
        }

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measureSumViaForLoop(BenchmarkState state, Blackhole blackhole){
        int result = 0;
        for (int i = 0; i < state.getIntegers().length; i++) {
            int value = state.getIntegers()[i];
            result += value;
        }
        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measureSumViaForEachLoop(BenchmarkState state, Blackhole blackhole){
        int result = 0;
        for (int value : state.getIntegers()) {
            result += value;
        }
        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measureSumViaParallelStream(BenchmarkState state, Blackhole blackhole){
        int result = IntStream.of(state.getIntegers())
                .boxed()
                .parallel()
                .mapToInt(x -> x.intValue())
                .sum();

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measuremSumViaSequentialStream(BenchmarkState state, Blackhole blackhole){
        int result = IntStream.of(state.getIntegers())
                .boxed()
                .mapToInt(x -> x.intValue())
                .sum();

        blackhole.consumeCPU(result);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StreamBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("StreamBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
