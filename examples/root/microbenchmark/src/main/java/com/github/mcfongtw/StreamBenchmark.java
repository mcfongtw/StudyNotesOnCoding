package com.github.mcfongtw;

import com.google.common.collect.Lists;
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
        private volatile List<Integer> integers = Lists.newArrayList();

        @Setup(Level.Trial)
        @Override
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
            for ( int i=0; i < SIZE ; i++){
                if ( i < SIZE / 2){
                    integers.add(i);
                }else {
                    integers.add(i-SIZE);
                }
            }
        }

        @TearDown(Level.Trial)
        @Override
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();
            integers.clear();
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
    public void measureEvenSumViaWhileIterator(BenchmarkState state, Blackhole blackhole){
        int result = 0;
        Iterator<Integer> iterator = state.getIntegers().iterator();
        while ( iterator.hasNext()){
            int value = iterator.next();
            if( value % 2 == 0) {
                result += value;
            }
        }

        assert result == -25_000_000;

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measureEvenSumViaForLoop(BenchmarkState state, Blackhole blackhole){
        int result = 0;
        for (int i = 0; i < state.getIntegers().size(); i++) {
            int value = state.getIntegers().get(i);
            if( value % 2 == 0) {
                result += value;
            }
        }

        assert result == -25_000_000;

        blackhole.consumeCPU( result);
    }

    @Benchmark
    public void measureEvenSumViaForEachLoop(BenchmarkState state, Blackhole blackhole){
        int result = 0;
        for (int value : state.getIntegers()) {
            if( value % 2 == 0) {
                result += value;
            }
        }

        assert result == -25_000_000;

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measureEvenSumViaParallelStream(BenchmarkState state, Blackhole blackhole){
        int result = state.getIntegers()
                .stream()
                .parallel()
                .filter( x -> x % 2 == 0)
                .mapToInt(x -> x.intValue())
                .sum();

        assert result == -25_000_000;

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measuremEvenSumViaSequentialStream(BenchmarkState state, Blackhole blackhole){
        int result = state.getIntegers()
                .stream()
                .filter( x -> x % 2 == 0)
                .mapToInt(x -> x.intValue())
                .sum();

        assert result == -25_000_000;

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measureTotalSumViaParallelStream(BenchmarkState state, Blackhole blackhole){
        int result = state.getIntegers()
                .stream()
                .parallel()
                .mapToInt(x -> x.intValue())
                .sum();

        assert result == -25_000_000;

        blackhole.consumeCPU(result);
    }

    @Benchmark
    public void measuremTotalSumViaSequentialStream(BenchmarkState state, Blackhole blackhole){
        int result = state.getIntegers()
                .stream()
                .mapToInt(x -> x.intValue())
                .sum();

        assert result == -25_000_000;

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
