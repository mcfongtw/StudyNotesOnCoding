package com.github.mcfongtw;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

interface Computatable {
    /**
     * calculate sum of an integer array
     * @param numbers
     * @return
     */
    long sum(int[] numbers);

    /**
     * shutdown pool or reclaim any related resources
     */
    void shutdown();
}

class SingleThreadAdder implements Computatable {
    public long sum(int[] numbers) {
        long total = 0L;
        for (int i : numbers) {
            total += i;
        }
        return total;
    }

    @Override
    public void shutdown() {
        // nothing to do
    }
}

class MultiThreadAdder implements Computatable {
    private final int nThreads;
    private final ExecutorService pool;

    public MultiThreadAdder(int nThreads) {
        this.nThreads = nThreads;
        this.pool = Executors.newFixedThreadPool(nThreads);
    }

    private class SumTask implements Callable<Long> {
        private int[] numbers;
        private int from;
        private int to;

        public SumTask(int[] numbers, int from, int to) {
            this.numbers = numbers;
            this.from = from;
            this.to = to;
        }

        public Long call() throws Exception {
            long total = 0L;
            for (int i = from; i < to; i++) {
                total += numbers[i];
            }
            return total;
        }
    }

    public long sum(int[] numbers) {
        int chunk = numbers.length / nThreads;

        int from, to;
        List<SumTask> tasks = new ArrayList<SumTask>();
        for (int i = 1; i <= nThreads; i++) {
            if (i == nThreads) {
                from = (i - 1) * chunk;
                to = numbers.length;
            } else {
                from = (i - 1) * chunk;
                to = i * chunk;
            }
            tasks.add(new SumTask(numbers, from, to));
        }

        try {
            List<Future<Long>> futures = pool.invokeAll(tasks);

            long total = 0L;
            for (Future<Long> future : futures) {
                total += future.get();
            }
            return total;
        } catch (Exception e) {
            // ignore
            return 0;
        }
    }

    @Override
    public void shutdown() {
        pool.shutdown();
    }
}

public class IntroToJmhBenchmark {

    private static final int NUM_ITERATION = 10;


    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        @Param({"10000", "100000", "1000000"})
        public int numOfElements;

        public int[] nums;
        public Computatable singleThreadAdder;
        public Computatable multiThreadAdder;

        @Setup
        public void prepare() {
            nums = IntStream.rangeClosed(1, numOfElements).toArray();
            singleThreadAdder = new SingleThreadAdder();
            multiThreadAdder = new MultiThreadAdder(Runtime.getRuntime().availableProcessors());
        }

        @TearDown
        public void shutdown() {
            singleThreadAdder.shutdown();
            multiThreadAdder.shutdown();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Measurement(iterations = NUM_ITERATION)
    @Warmup(iterations = 5)
    public long measureSingleThreadedComputation(ExecutionPlan executionPlan) {
        return executionPlan.singleThreadAdder.sum(executionPlan.nums);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Measurement(iterations = NUM_ITERATION)
    @Warmup(iterations = 5)
    public long measureMultiThreadedComputation(ExecutionPlan executionPlan) {
        return executionPlan.multiThreadAdder.sum(executionPlan.nums);
    }

    @Benchmark
    @BenchmarkMode({Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Measurement(iterations = 1)
    @Warmup(iterations = 0)
    public long measureSingleShotComputation(ExecutionPlan executionPlan) {
        return executionPlan.singleThreadAdder.sum(executionPlan.nums);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION)
    @Warmup(iterations = 5)
    public void measureDeadCodeElimination() {
        Math.log(Math.PI);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION)
    @Warmup(iterations = 5)
    public void measureAvoidingDeadCodeElimination(Blackhole blackhole) {
        blackhole.consume(Math.log(Math.PI));
    }


    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(IntroToJmhBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .forks(2)
                .resultFormat(ResultFormatType.JSON)
                .result("IntroToJmhBenchmark-result.json")
                .build();

        Collection<RunResult> runResultCollection = new Runner(opt).run();
    }
}
