package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public class FileReplicationBenchmark extends AbstractIoBenchmark {

    public static Logger LOG = LoggerFactory.getLogger(FileReplicationBenchmark.class);


    @State(Scope.Benchmark)
    public static class FileReplicationExecutionPlan extends AbstractSequentialExecutionPlan {

        LatencyMetric ioLatencyMetric = new LatencyMetric(FileReplicationExecutionPlan.class.getName());

        //1MB, 10MB, 100MB, 1GB
        @Param({"1048576", "10485760", "104857600", "1073741824"})
        public int fileSize;

        @Override
        @Setup(Level.Trial)
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
        }

        @Override
        public void preTrialSetUp() throws Exception {
            super.preTrialSetUp();

            tempDir = Files.createTempDir();
            new File(tempDir.getAbsolutePath()).mkdirs();

            finPath = tempDir.getAbsolutePath() + "/in.data";
            foutPath = tempDir.getAbsolutePath() + "/out.data";

            FileUtils.touch(new File(finPath));
            FileUtils.touch(new File(foutPath));

            //Sequential data generation
            try(
                    FileOutputStream fin = new FileOutputStream(finPath);
            ) {
                for(int i = 0; i < fileSize; i++) {
                    fin.write((byte) i);
                }
            }

            logger.debug("Temp dir created at [{}]", tempDir.getAbsolutePath());
            logger.debug("File created at [{}]", finPath);
            logger.debug("File created at [{}]", foutPath);
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
            super.doIterationTearDown();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void zeroTransferToCopy(FileReplicationExecutionPlan plan) throws Exception {
        try (
                RandomAccessFile fromFile = new RandomAccessFile(plan.finPath, "r");
                RandomAccessFile toFile = new RandomAccessFile(plan.foutPath, "rwd");
                FileChannel fromChannel = fromFile.getChannel();
                FileChannel toChannel = toFile.getChannel();
        ) {

            int fromLength = (int) fromChannel.size();

            long beforeTime = System.nanoTime();

            long returnCode = fromChannel.transferTo(0, fromLength, toChannel);
            if(returnCode >= 0 ) {
                LOG.debug("transferTo [{}] / [{}] bytes", new Object[]{fromLength, fromLength});
            } else {
                LOG.warn("transferTo failed! error code: [{}]", returnCode);
            }

            assert fromFile.length() == toFile.length();

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void nioFilesCopy(FileReplicationExecutionPlan plan) throws Exception {
        Path srcPath = Paths.get(plan.finPath);
        Path dstPath = Paths.get(plan.foutPath);

        long beforeTime = System.nanoTime();

        java.nio.file.Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);

        assert srcPath.toFile().length()== dstPath.toFile().length();

        long afterTime = System.nanoTime();
        plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void commonIoFilesCopy(FileReplicationExecutionPlan plan) throws Exception {
        File srcFile = new File(plan.finPath);
        File dstFile = new File(plan.foutPath);

        long beforeTime = System.nanoTime();

        FileUtils.copyFile(srcFile, dstFile);

        assert srcFile.length()== dstFile.length();

        long afterTime = System.nanoTime();
        plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void guavaFilesCopy(FileReplicationExecutionPlan plan) throws Exception {
        File srcFile = new File(plan.finPath);
        File dstFile = new File(plan.foutPath);

        long beforeTime = System.nanoTime();

        com.google.common.io.Files.copy(srcFile, dstFile);

        assert srcFile.length()== dstFile.length();

        long afterTime = System.nanoTime();
        plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(FileReplicationBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(0)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("FileReplicationBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }

}
