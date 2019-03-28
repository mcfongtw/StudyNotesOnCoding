package com.github.mcfongtw.io.file;

import com.github.mcfongtw.io.AbstractIoBenchmarkBase;
import com.github.mcfongtw.metrics.LatencyMetric;
import lombok.Getter;
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

public class ByteByByteReplicationBenchmark extends AbstractIoBenchmarkBase {

    public static Logger LOG = LoggerFactory.getLogger(ByteByByteReplicationBenchmark.class);

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractSequentialIoBenchmarkLifecycle {

        private LatencyMetric ioLatencyMetric = new LatencyMetric(ByteByByteReplicationBenchmark.class.getName());

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
    public void copyWithFileStream(BenchmarkState state) throws IOException {
        try(
                FileInputStream fin = new FileInputStream(state.getFinPath());
                FileOutputStream fout = new FileOutputStream(state.getFoutPath());
        ) {
            long beforeTime = System.nanoTime();

            int byteRead = 0;
            while ((byteRead = fin.read()) != -1) {
                fout.write(byteRead);
            }

            assert new File(state.getFinPath()).length() == new File(state.getFoutPath()).length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }



    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithRandomAccessFile(BenchmarkState state) throws IOException {
        try(
            RandomAccessFile fout = new RandomAccessFile(state.getFoutPath(), "rw");
            RandomAccessFile fin = new RandomAccessFile(state.getFinPath(), "r");
        ) {
            long beforeTime = System.nanoTime();


            for (int finIdx = 0; finIdx < fin.length(); finIdx++) {
                fin.seek(finIdx);
                fout.writeByte(fin.readByte());
            }

            assert fin.length() == fout.length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
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
