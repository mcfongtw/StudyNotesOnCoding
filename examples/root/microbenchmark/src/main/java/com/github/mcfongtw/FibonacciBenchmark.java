package com.github.mcfongtw;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FibonacciBenchmark {

    private static final int NUM_ITERATION = 1;

    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        @Param({"10", "50"})
        public int FIB_NUMBER_INDEX;
    }

    /*          1              , if x = 1
     * f(x) =   1              , if x = 2
     *        f(x-1) + f(x - 2), else
     */
    @VisibleForTesting
    static int doRecursion(int x) {
        return (x == 0 || x == 1) ? x : doRecursion(x - 1) + doRecursion(x - 2);
    }

    @VisibleForTesting
    static int doTailRecursive(int x) {
        if(x <= 2)
            return 2;
        else
            return fibTailRec(x, 0,1);
    }

    private static int fibTailRec(int x, int fMinus1, int fMinus2) {
        if(x == 0) return fMinus1;
        return fibTailRec(x - 1, fMinus2, fMinus1 + fMinus2);
    }


    private static int fibMemoization(int x, int[] mem) {
        if (mem[x] != 0) return mem[x];
        if (x == 1 || x == 2)  return 1;
        int n = fibMemoization(x - 1, mem) + fibMemoization(x - 2,mem);
        mem[x] = n;
        return n;
    }

    @VisibleForTesting
    static int doDpTopDown(int x) {
        return fibMemoization(x, new int[x + 1]);
    }


    @VisibleForTesting
    static int doDpBottomUp(int x) {
        if (x == 0 || x == 1) return 1;
        int[] memory = new int[x + 1];
        memory[1] = 1;
        memory[2] = 1;
        for (int i = 3; i <= x; i++) memory[i] = memory[i - 1] + memory[i - 2];
        return memory[x];
    }

    @VisibleForTesting
    static int doJava8Stream(int n) {
        return Stream.iterate(new Integer[]{0, 1}, s -> new Integer[]{s[1], s[0]+s[1]})
                .limit(n)
                .reduce((x, y) -> y).orElse(null)[1];
    }

    @VisibleForTesting
    static int doRxStream(int x) {
        return Observable.fromArray(0)
                .repeat()
                .scan(new int[]{0, 1}, (a, b) -> new int[]{a[1], a[0] + a[1]})
                .map(a -> a[1])
                .take(x)
                .blockingLast();
    }


    @VisibleForTesting
    static int doReactorStream(int x) {
        List<Integer> sequence = Lists.newArrayList();
        Flux<Integer> integerFlux = Flux.generate(
                () -> Tuples.<Integer, Integer>of(1, 1),
                (state, sink) -> {
                    sink.next(state.getT1());
                    return Tuples.of(state.getT2(), state.getT1() + state.getT2());
                });

        integerFlux
                .take(x)
                .subscribe(number -> sequence.add(number));
        return sequence.get(x - 1);
    }


    @VisibleForTesting
    static int doIterative(int n) {
        if(n <= 1) {
            return n;
        }
        int fib = 1;
        int prevFib = 1;

        for(int i=2; i<n; i++) {
            int temp = fib;
            fib+= prevFib;
            prevFib = temp;
        }
        return fib;
    }




    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureRecursive(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doRecursion(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureTailRecursive(ExecutionPlan executionPlan, Blackhole blackhole){
        blackhole.consume(doTailRecursive(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureIterative(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doIterative(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureDynamicProgrammingTopDown(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doDpTopDown(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureDynamicProgrammingBottomUp(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doDpBottomUp(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureJavaStream(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doJava8Stream(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureRxStream(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doRxStream(executionPlan.FIB_NUMBER_INDEX));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations=NUM_ITERATION)
    public void measureReactorStream(ExecutionPlan executionPlan, Blackhole blackhole) {
        blackhole.consume(doReactorStream(executionPlan.FIB_NUMBER_INDEX));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FibonacciBenchmark.class.getSimpleName())
                .forks(3)
                .warmupIterations(5)
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("FibonacciBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
