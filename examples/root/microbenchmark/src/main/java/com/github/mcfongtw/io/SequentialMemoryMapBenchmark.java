package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class SequentialMemoryMapBenchmark extends AbstractIoBenchmark {

    public static Logger LOG = LoggerFactory.getLogger(SequentialMemoryMapBenchmark.class);

    @State(Scope.Benchmark)
    public static class SequentialMemoryMapExecutionPlan extends AbstractSequentialExecutionPlan {

        LatencyMetric ioLatencyMetric = new LatencyMetric(SequentialMemoryMapBenchmark.class.getName());

        @Param({"4096", "10240", "102400"})
        public int bufferCapacity;

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
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void doMemoryMapBackedStreamWithVariedBufferCapacity(SequentialMemoryMapExecutionPlan plan) throws IOException {
        try (
            RandomAccessFile fin = new RandomAccessFile(plan.finPath, "r");
            RandomAccessFile fout = new RandomAccessFile(plan.foutPath, "rw");
            FileChannel finChannel = fin.getChannel();
            FileChannel foutChannel = fout.getChannel();
        ) {

            int finLength = (int) finChannel.size();

            MappedByteBuffer bufIn = finChannel.map(FileChannel.MapMode.READ_ONLY, 0, finLength);
            MappedByteBuffer bufOut = foutChannel.map(FileChannel.MapMode.READ_WRITE, 0, finLength);

            long beforeTime = System.nanoTime();

            for (int bufIndex = 0; bufIndex < finLength; ) {
                int bufLength = 0;

                if (bufIndex + plan.bufferCapacity > finLength) {
                    bufLength = finLength % plan.bufferCapacity;
                } else {
                    bufLength = plan.bufferCapacity;
                }

                byte buffer[] = new byte[bufLength];
                bufIn.get(buffer, 0, bufLength);
                bufOut.put(buffer);

                bufIndex += plan.bufferCapacity;

                LOG.debug("mmapped [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, plan.bufferCapacity});
            }

            assert fin.length() == fout.length();

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void doFileBackedStreamWithVariedLocalBuffer(SequentialMemoryMapExecutionPlan plan) throws IOException {
        try (
            RandomAccessFile fin = new RandomAccessFile(plan.finPath, "r");
            RandomAccessFile fout = new RandomAccessFile(plan.foutPath, "rw");

        ) {
            long beforeTime = System.nanoTime();

            byte[] buffer = new byte[plan.bufferCapacity];

            int bufLength = fin.read(buffer);

            while (bufLength > 0) {
                if (bufLength == buffer.length) {
                    fout.write(buffer);
                } else {
                    fout.write(buffer, 0, bufLength);
                }

                //LOG.debug("mmapped [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex + bufLength, finLength, plan.bufferCapacity});

                bufLength = fin.read(buffer);
            }

            assert fin.length() == fout.length();

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void doFileBackedStreamByteByByte(SequentialMemoryMapExecutionPlan plan) throws IOException {
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
                .include(SequentialMemoryMapBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(0)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
