package com.github.mcfongtw;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FibonacciBenchmark {

    private static final int STARTING_FIB_NUMBER = 45;

    private int fibNaiveRecursive(int x) {
        return (x == 1 || x == 2)?1:fibNaiveRecursive(x - 1) + fibNaiveRecursive(x - 2);
    }

    private int fibTailRecursive(int x) {
        return fibTailRec(x, 0,1);
    }

    private int fibTailRec(int n, int a, int b) {
        if (n == 0) return a;
        if (n == 1) return b;
        return fibTailRec(n - 1, b, a + b);
    }

    private int fibMemoization(int x, int[] mem) {
        if (mem[x] != 0) return mem[x];
        if (x == 1 || x == 2)  return 1;
        int n = fibMemoization(x - 1, mem) + fibMemoization(x - 2,mem);
        mem[x] = n;
        return n;
    }

    private int fibBottomUp(int x) {
        if (x == 1 || x == 2) return 1;
        int[] memory = new int[x + 1];
        memory[1] = 1;
        memory[2] = 1;
        for (int i = 3; i <= x; i++) memory[i] = memory[i - 1] + memory[i - 2];
        return memory[x];
    }

    private int fibStream(int n) {
        return Stream.iterate(new Integer[]{0, 1}, s -> new Integer[]{s[1], s[0]+s[1]})
                .limit(n)
                .reduce((x, y) -> y).orElse(null)[1];
    }


    public int fibIterative(int n) {
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
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations=10)
    public void naiveIterative() {
        fibIterative(STARTING_FIB_NUMBER);
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations=10)
    public void naiveRecursive() {
        fibNaiveRecursive(STARTING_FIB_NUMBER);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations=10)
    public void tailRecursive(){
        fibTailRecursive(STARTING_FIB_NUMBER);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations=10)
    public void memoization() {
        fibMemoization(STARTING_FIB_NUMBER,new int[STARTING_FIB_NUMBER+1]);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations=10)
    public void bottomUp() {
        fibBottomUp(STARTING_FIB_NUMBER);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations=10)
    public void stream() {
        fibStream(STARTING_FIB_NUMBER);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FibonacciBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("FibonacciBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
