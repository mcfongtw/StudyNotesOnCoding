package com.github.mcfongtw.io.file;

import com.github.mcfongtw.io.AbstractIoBenchmarkBase;
import com.github.mcfongtw.metrics.LatencyMetric;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 20)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class FileReplicationBenchmark extends AbstractIoBenchmarkBase {

    public static Logger LOG = LoggerFactory.getLogger(FileReplicationBenchmark.class);

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractReplicationIoBenchmarkLifecycle {

        private LatencyMetric ioLatencyMetric = new LatencyMetric(FileReplicationBenchmark.class.getName());

        //1MB, 10MB, 100MB
        @Param({"1048576", "10485760", "104857600"})
        protected int paramFileSize;

        @Override
        public void preTrialSetUp() throws Exception {
            fileSize = paramFileSize;
            super.preTrialSetUp();
        }

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
    public void zeroTransferToCopy(BenchmarkState state, Blackhole blackhole) throws Exception {
        try (
                RandomAccessFile fromFile = new RandomAccessFile(state.getFinPath(), "r");
                RandomAccessFile toFile = new RandomAccessFile(state.getFoutPath(), "rw");
                FileChannel fromChannel = fromFile.getChannel();
                FileChannel toChannel = toFile.getChannel();
        ) {

            long fromLength = fromChannel.size();

            long beforeTime = System.nanoTime();

            long returnCode = fromChannel.transferTo(0, fromLength, toChannel);
            if(returnCode >= 0 ) {
                LOG.trace("transferTo [{}] / [{}] bytes", new Object[]{fromLength, fromLength});
            } else {
                LOG.warn("transferTo failed! error code: [{}]", returnCode);
            }

            assert fromFile.length() == toFile.length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

            blackhole.consume(afterTime - beforeTime);
        }
    }

    @Benchmark
    public void nioFilesCopy(BenchmarkState state, Blackhole blackhole) throws Exception {
        Path srcPath = Paths.get(state.getFinPath());
        Path dstPath = Paths.get(state.getFoutPath());

        long beforeTime = System.nanoTime();

        java.nio.file.Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);

        assert srcPath.toFile().length()== dstPath.toFile().length();

        long afterTime = System.nanoTime();
        state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

        blackhole.consume(afterTime - beforeTime);
    }

    @Benchmark
    public void commonIoFilesCopy(BenchmarkState state, Blackhole blackhole) throws Exception {
        File srcFile = new File(state.getFinPath());
        File dstFile = new File(state.getFoutPath());

        long beforeTime = System.nanoTime();

        FileUtils.copyFile(srcFile, dstFile);

        assert srcFile.length()== dstFile.length();

        long afterTime = System.nanoTime();
        state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

        blackhole.consume(afterTime - beforeTime);
    }

    @Benchmark
    public void guavaFilesCopy(BenchmarkState state, Blackhole blackhole) throws Exception {
        File srcFile = new File(state.getFinPath());
        File dstFile = new File(state.getFoutPath());

        long beforeTime = System.nanoTime();

        com.google.common.io.Files.copy(srcFile, dstFile);

        assert srcFile.length()== dstFile.length();

        long afterTime = System.nanoTime();
        state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

        blackhole.consume(afterTime - beforeTime);
    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(FileReplicationBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("FileReplicationBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }

}
