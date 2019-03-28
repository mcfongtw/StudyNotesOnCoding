package com.github.mcfongtw;

import com.google.common.annotations.VisibleForTesting;
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

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class FibonacciBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {
        @Param({"10", "50"})
        public int FIB_NUMBER_INDEX;


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
        return Stream
                .iterate(new int[]{0, 1}, f -> new int[]{f[1], f[0] + f[1]})
                .limit(n)
                .reduce((x, y) -> y)
                .orElse(null)[1];
    }

    @VisibleForTesting
    static int doRxStream(int x) {
        return Observable
                .fromArray(0) // base condition
                .repeat()
                .scan(new int[]{0, 1}, (f, b) -> new int[]{f[1], f[0] + f[1]})
                .map(f -> f[1])
                .take(x)
                .blockingLast();
    }


    @VisibleForTesting
    static int doReactorStream(int x) {
        return Flux
                .fromArray(new Integer[]{0}) // base condition
                .repeat()
                .scan(new int[]{0, 1}, (f, b) -> new int[]{f[1], f[0] + f[1]})
                .map(f -> f[1])
                .take(x)
                .blockLast();
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
    public void measureRecursive(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doRecursion(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureTailRecursive(BenchmarkState benchmarkState, Blackhole blackhole){
        blackhole.consume(doTailRecursive(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureIterative(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doIterative(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureDynamicProgrammingTopDown(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doDpTopDown(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureDynamicProgrammingBottomUp(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doDpBottomUp(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureJavaStream(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doJava8Stream(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureRxStream(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doRxStream(benchmarkState.FIB_NUMBER_INDEX));
    }

    @Benchmark
    public void measureReactorStream(BenchmarkState benchmarkState, Blackhole blackhole) {
        blackhole.consume(doReactorStream(benchmarkState.FIB_NUMBER_INDEX));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FibonacciBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("FibonacciBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
