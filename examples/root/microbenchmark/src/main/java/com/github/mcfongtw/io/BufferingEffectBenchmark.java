package com.github.mcfongtw.io;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class BufferingEffectBenchmark extends AbstractIoBenchmark {

    @State(Scope.Benchmark)
    public static class BufferingEffectExecutionPlan extends AbstractExecutionPlan {

        String finPath;

        String foutPath;

        File tempDir;

        InfluxdbLatencyMetric ioLatencyMetric = new InfluxdbLatencyMetric(BufferingEffectBenchmark.class.getName());

        @Param({"512", "4096", "10240"})
        public int bufferSize;


        @Setup(Level.Trial)
        public void setUp() throws IOException {
            super.setUp();
            tempDir = Files.createTempDir();
            new File(tempDir.getAbsolutePath()).mkdirs();
            logger.debug("Temp dir created at [{}] for BufferSize {}", tempDir.getAbsolutePath(), bufferSize);
        }

        @TearDown(Level.Trial)
        public void tearDown() throws IOException, InterruptedException {
            super.tearDown();
            FileUtils.deleteDirectory(tempDir);
            logger.debug("Temp dir deleted at [{}] for BufferSize {}", tempDir.getAbsolutePath(), bufferSize);

        }

        @Setup(Level.Iteration)
        public void iterate() throws IOException {
            finPath = tempDir.getAbsolutePath() + "/in.data";
            foutPath = tempDir.getAbsolutePath() + "/out.data";
            logger.debug("File created at [{}] for BufferSize {}", finPath, bufferSize);
            logger.debug("File created at [{}] for BufferSize {}", foutPath, bufferSize);

            FileUtils.touch(new File(finPath));
            FileUtils.touch(new File(foutPath));
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void doFileReadWritePerByteBenchmark(BufferingEffectExecutionPlan plan) throws IOException {
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
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void doFileReadWriteWithLocalBufferBenchmark(BufferingEffectExecutionPlan plan) throws IOException {
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
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void doBufferedFileReadWriteBenchmark(BufferingEffectExecutionPlan plan) throws IOException {
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
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(BufferingEffectBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(0)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
