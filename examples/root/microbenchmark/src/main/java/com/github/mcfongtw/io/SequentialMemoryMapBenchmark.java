package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class SequentialMemoryMapBenchmark extends AbstractIoBenchmark {

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
    @Measurement(iterations = NUM_ITERATION, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void doHeapMemoryBackedChannelStreamWithVariedBufferCapacity(SequentialMemoryMapExecutionPlan plan) throws IOException {
        FileChannel fout = new RandomAccessFile(plan.foutPath, "rw").getChannel();
        FileChannel fin = new RandomAccessFile(plan.finPath, "r").getChannel();

        ByteBuffer bufIn = ByteBuffer.allocate(plan.bufferCapacity);
        ByteBuffer bufOut = ByteBuffer.allocate(plan.bufferCapacity);

        long beforeTime = System.nanoTime();

        for(int i = 0; i < plan.bufferCapacity; i++) {
            bufOut.put(bufIn.get(plan.bufferCapacity - i - 1));
        }

        long afterTime = System.nanoTime();
        plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

        fin.close();
        fout.close();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void doDirectMemoryBackedChannelStreamWithVariedBufferCapacity(SequentialMemoryMapExecutionPlan plan) throws IOException {
        FileChannel fout = new RandomAccessFile(plan.foutPath, "rw").getChannel();
        FileChannel fin = new RandomAccessFile(plan.finPath, "r").getChannel();

        ByteBuffer bufIn = ByteBuffer.allocateDirect(plan.bufferCapacity);
        ByteBuffer bufOut = ByteBuffer.allocateDirect(plan.bufferCapacity);

        long beforeTime = System.nanoTime();

        for(int i = 0; i < plan.bufferCapacity; i++) {
            bufOut.put(bufIn.get(plan.bufferCapacity - i - 1));
        }

        long afterTime = System.nanoTime();
        plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

        fin.close();
        fout.close();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void doFileBackedStreamByteByByte(SequentialMemoryMapExecutionPlan plan) throws IOException {
        RandomAccessFile fout = new RandomAccessFile(plan.foutPath, "rw");
        RandomAccessFile fin = new RandomAccessFile(plan.finPath, "r");

        long beforeTime = System.nanoTime();


        for(int i = 0; i < plan.bufferCapacity; i++) {
            fin.seek(fin.length()-4);
            fout.writeInt(fin.readInt());
        }

        long afterTime = System.nanoTime();
        plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);

        fin.close();
        fout.close();
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
