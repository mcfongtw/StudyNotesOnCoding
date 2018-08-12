package com.github.mcfongtw.io;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class SequentialFileStreamBenchmark extends AbstractIoBenchmark {

    @State(Scope.Benchmark)
    public static class SequentialFileStreamExecutionPlan extends AbstractSequentialExecutionPlan {

        InfluxdbLatencyMetric ioLatencyMetric = new InfluxdbLatencyMetric(SequentialFileStreamBenchmark.class.getName());

        @Param({"512", "4096", "10240"})
        public int bufferSize;

        @Override
        @Setup(Level.Trial)
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
        }

        @Override
        @TearDown(Level.Trial)
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();
        }

        @Override
        @Setup(Level.Iteration)
        public void doIterationSetup() throws Exception {
            super.doIterationSetup();
        }

        @Override
        @TearDown(Level.Iteration)
        public void doIterationTearDown() throws Exception {
            super.doIterationSetup();
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 2, timeUnit = TimeUnit.SECONDS)
    public void doFileStreamByteByByte(SequentialFileStreamExecutionPlan plan) throws IOException {
        try(
                FileInputStream fin = new FileInputStream(plan.finPath);
                FileOutputStream fout = new FileOutputStream(plan.foutPath);
        ) {
            long beforeTime = System.nanoTime();

            int byteRead = 0;
            while ((byteRead = fin.read()) != -1) {
                fout.write(byteRead);
            }

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void doFileStreamWithLocalBufferOfVariedSize(SequentialFileStreamExecutionPlan plan) throws IOException {
        try(
                FileInputStream fin = new FileInputStream(plan.finPath);
                FileOutputStream fout = new FileOutputStream(plan.foutPath);
        ) {
            long beforeTime = System.nanoTime();

            byte[] buffer = new byte[plan.bufferSize];
            int numBytesRead = 0;
            while ((numBytesRead = fin.read(buffer)) != -1) {
                fout.write(buffer, 0, numBytesRead);
            }

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void doBufferedFileStreamWithVariedBufferSize(SequentialFileStreamExecutionPlan plan) throws IOException {
        try(
                BufferedInputStream fin = new BufferedInputStream(new FileInputStream(plan.finPath), plan.bufferSize);
                BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(plan.foutPath), plan.bufferSize);
        ) {
            long beforeTime = System.nanoTime();

            int byteRead = 0;
            while ((byteRead = fin.read()) != -1) {
                fout.write(byteRead);
            }

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }


    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(SequentialFileStreamBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(0)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
