package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class ByteByByteReplicationBenchmark extends AbstractIoBenchmark {

    public static Logger LOG = LoggerFactory.getLogger(ByteByByteReplicationBenchmark.class);

    @State(Scope.Benchmark)
    public static class ByteByByteReplicationExecutionPlan extends AbstractSequentialExecutionPlan {

        LatencyMetric ioLatencyMetric = new LatencyMetric(ByteByByteReplicationBenchmark.class.getName());

        @Override
        @Setup(Level.Trial)
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
        }

        @Override
        @TearDown(Level.Trial)
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();

            assert tempDir.exists() == false;
        }

        @Override
        @Setup(Level.Iteration)
        public void doIterationSetup() throws Exception {
            super.doIterationSetup();
        }

        @Override
        @TearDown(Level.Iteration)
        public void doIterationTearDown() throws Exception {
            super.doIterationTearDown();
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 2, timeUnit = TimeUnit.SECONDS)
    public void copyWithFileStream(ByteByByteReplicationExecutionPlan plan) throws IOException {
        try(
                FileInputStream fin = new FileInputStream(plan.finPath);
                FileOutputStream fout = new FileOutputStream(plan.foutPath);
        ) {
            long beforeTime = System.nanoTime();

            int byteRead = 0;
            while ((byteRead = fin.read()) != -1) {
                fout.write(byteRead);
            }

            assert new File(plan.finPath).length() == new File(plan.foutPath).length();

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }



    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithRandomAccessFile(ByteByByteReplicationExecutionPlan plan) throws IOException {
        try(
            RandomAccessFile fout = new RandomAccessFile(plan.foutPath, "rw");
            RandomAccessFile fin = new RandomAccessFile(plan.finPath, "r");
        ) {
            long beforeTime = System.nanoTime();


            for (int finIdx = 0; finIdx < fin.length(); finIdx++) {
                fin.seek(finIdx);
                fout.writeByte(fin.readByte());
            }

            assert fin.length() == fout.length();

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }

    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(ByteByByteReplicationBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(10)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("ByteByByteReplicationBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
