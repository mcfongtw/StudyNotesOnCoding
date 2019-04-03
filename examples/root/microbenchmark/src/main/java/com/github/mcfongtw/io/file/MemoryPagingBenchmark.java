package com.github.mcfongtw.io.file;

import com.github.mcfongtw.io.AbstractIoBenchmarkBase;
import com.github.mcfongtw.metrics.LatencyMetric;
import com.google.common.io.Files;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 20)
@Warmup(iterations = 5)
@Fork(3)
public class MemoryPagingBenchmark extends AbstractIoBenchmarkBase {

    public static Logger LOG = LoggerFactory.getLogger(MemoryPagingBenchmark.class);

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractPagingIoBenchmarkLifecycle {
        private LatencyMetric ioLatencyMetric = new LatencyMetric(MemoryPagingBenchmark.class.getName());
        
        //128, 1K, 4K, 128K, 1M
        @Param({"128", "4096", "131072", "1048576"})
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
    public void copyWithRawBuffer(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    FileInputStream fin = new FileInputStream(finPath);
                    FileOutputStream fout = new FileOutputStream(foutPath);
            ) {
                long beforeTime = System.nanoTime();

                byte[] buffer = new byte[state.BUFFER_SIZE];
                int numBytesRead = 0;
                while ((numBytesRead = fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, numBytesRead);
                }

                assert new File(finPath).length() == new File(foutPath).length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    public void copyWithBufferedFileStream(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    BufferedInputStream fin = new BufferedInputStream(new FileInputStream(finPath), state.BUFFER_SIZE);
                    BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(foutPath), state.BUFFER_SIZE);
            ) {
                long beforeTime = System.nanoTime();

                int byteRead = 0;
                while ((byteRead = fin.read()) != -1) {
                    fout.write(byteRead);
                }
                fout.flush();

                assert new File(finPath).length() == new File(foutPath).length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }


        }
    }

    @Benchmark
    public void copyWithFileChannel(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    FileChannel finChannel = new FileInputStream(finPath).getChannel();
                    FileChannel foutChannel = new FileOutputStream(foutPath).getChannel();
            ) {
                long beforeTime = System.nanoTime();
                int finLength = (int) finChannel.size();

                for (int bufIndex = 0; bufIndex < finLength; ) {
                    ByteBuffer buffer = ByteBuffer.allocate(state.BUFFER_SIZE);
                    int bufLength = 0;

                    if (bufIndex + state.BUFFER_SIZE > finLength) {
                        bufLength = finLength % state.BUFFER_SIZE;
                    } else {
                        bufLength = state.BUFFER_SIZE;
                    }

                    finChannel.read(buffer);

                    //switch to write mode for ByteBuffer
                    buffer.flip();
                    foutChannel.write(buffer);

                    bufIndex += bufLength;

                    LOG.trace("streamed [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, state.BUFFER_SIZE});
                }

                assert finChannel.size() == foutChannel.size();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    public void copyWithMmap(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);

            try (

                    RandomAccessFile fin = new RandomAccessFile(finPath, "r");
                    RandomAccessFile fout = new RandomAccessFile(foutPath, "rw");
                    FileChannel finChannel = fin.getChannel();
                    FileChannel foutChannel = fout.getChannel();
            ) {

                int finLength = (int) finChannel.size();

                MappedByteBuffer bufIn = finChannel.map(FileChannel.MapMode.READ_ONLY, 0, finLength);
                MappedByteBuffer bufOut = foutChannel.map(FileChannel.MapMode.READ_WRITE, 0, finLength);

                long beforeTime = System.nanoTime();

                for (int bufIndex = 0; bufIndex < finLength; ) {
                    int bufLength = 0;

                    if (bufIndex + state.BUFFER_SIZE > finLength) {
                        bufLength = finLength % state.BUFFER_SIZE;
                    } else {
                        bufLength = state.BUFFER_SIZE;
                    }

                    byte buffer[] = new byte[bufLength];
                    bufIn.get(buffer, 0, bufLength);
                    bufOut.put(buffer);

                    bufIndex += state.BUFFER_SIZE;

                    LOG.trace("mmapped [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, state.BUFFER_SIZE});
                }

                assert fin.length() == fout.length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }

    }

    @Benchmark
    public void copyWithMmapAndFsync(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);

            try (

                    RandomAccessFile fin = new RandomAccessFile(finPath, "r");
                    RandomAccessFile fout = new RandomAccessFile(foutPath, "rws");
                    FileChannel finChannel = fin.getChannel();
                    FileChannel foutChannel = fout.getChannel();
            ) {

                int finLength = (int) finChannel.size();

                MappedByteBuffer bufIn = finChannel.map(FileChannel.MapMode.READ_ONLY, 0, finLength);
                MappedByteBuffer bufOut = foutChannel.map(FileChannel.MapMode.READ_WRITE, 0, finLength);

                long beforeTime = System.nanoTime();

                for (int bufIndex = 0; bufIndex < finLength; ) {
                    int bufLength = 0;

                    if (bufIndex + state.BUFFER_SIZE > finLength) {
                        bufLength = finLength % state.BUFFER_SIZE;
                    } else {
                        bufLength = state.BUFFER_SIZE;
                    }

                    byte buffer[] = new byte[bufLength];
                    bufIn.get(buffer, 0, bufLength);
                    bufOut.put(buffer);

                    bufIndex += state.BUFFER_SIZE;

                    LOG.trace("mmapped [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, state.BUFFER_SIZE});
                }

                assert fin.length() == fout.length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }

    }

    @Benchmark
    public void copyWithRawBufferedRandomAccessFile(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    RandomAccessFile fin = new RandomAccessFile(finPath, "r");
                    RandomAccessFile fout = new RandomAccessFile(foutPath, "rw");

            ) {
                long beforeTime = System.nanoTime();

                byte[] buffer = new byte[state.BUFFER_SIZE];

                int bufLength = fin.read(buffer);

                while (bufLength > 0) {
                    if (bufLength == buffer.length) {
                        fout.write(buffer);
                    } else {
                        fout.write(buffer, 0, bufLength);
                    }

                    bufLength = fin.read(buffer);
                }

                assert fin.length() == fout.length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    public void copyWithRawBufferedRandomAccessFileAndFsync(BenchmarkState state) throws IOException {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    RandomAccessFile fin = new RandomAccessFile(finPath, "r");
                    RandomAccessFile fout = new RandomAccessFile(foutPath, "rws");

            ) {
                long beforeTime = System.nanoTime();

                byte[] buffer = new byte[state.BUFFER_SIZE];

                int bufLength = fin.read(buffer);

                while (bufLength > 0) {
                    if (bufLength == buffer.length) {
                        fout.write(buffer);
                    } else {
                        fout.write(buffer, 0, bufLength);
                    }

                    bufLength = fin.read(buffer);
                }

                assert fin.length() == fout.length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    public void zeroTransferToCopy(BenchmarkState state) throws Exception {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    RandomAccessFile fromFile = new RandomAccessFile(finPath, "r");
                    RandomAccessFile toFile = new RandomAccessFile(foutPath, "rw");
                    FileChannel fromChannel = fromFile.getChannel();
                    FileChannel toChannel = toFile.getChannel();
            ) {

                int fromLength = (int) fromChannel.size();

                long beforeTime = System.nanoTime();

                for (int toIndex = 0; toIndex < fromLength; ) {
                    int bufLength = 0;

                    if (toIndex + state.BUFFER_SIZE > fromLength) {
                        bufLength = fromLength % state.BUFFER_SIZE;
                    } else {
                        bufLength = state.BUFFER_SIZE;
                    }


                    long returnCode = fromChannel.transferTo(toIndex, bufLength, toChannel);
                    if (returnCode >= 0) {
                        LOG.trace("transferTo [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{toIndex, fromLength, state.BUFFER_SIZE});
                    } else {
                        LOG.warn("transferTo failed! error code: [{}]", returnCode);
                    }

                    toIndex += state.BUFFER_SIZE;
                }


                assert fromFile.length() == toFile.length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    public void zeroTransferToCopyAndFsync(BenchmarkState state) throws Exception {
        for(int index = 0; index < state.MAX_NUM_FILES; index++) {
            String finPath = state.getListOfFinPath().get(index);
            String foutPath = state.getListOfFoutPath().get(index);
            try (
                    RandomAccessFile fromFile = new RandomAccessFile(finPath, "r");
                    RandomAccessFile toFile = new RandomAccessFile(foutPath, "rws");
                    FileChannel fromChannel = fromFile.getChannel();
                    FileChannel toChannel = toFile.getChannel();
            ) {

                int fromLength = (int) fromChannel.size();

                long beforeTime = System.nanoTime();

                for (int toIndex = 0; toIndex < fromLength; ) {
                    int bufLength = 0;

                    if (toIndex + state.BUFFER_SIZE > fromLength) {
                        bufLength = fromLength % state.BUFFER_SIZE;
                    } else {
                        bufLength = state.BUFFER_SIZE;
                    }


                    long returnCode = fromChannel.transferTo(toIndex, bufLength, toChannel);
                    if (returnCode >= 0) {
                        LOG.trace("transferTo [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{toIndex, fromLength, state.BUFFER_SIZE});
                    } else {
                        LOG.warn("transferTo failed! error code: [{}]", returnCode);
                    }

                    toIndex += state.BUFFER_SIZE;
                }


                assert fromFile.length() == toFile.length();

                long afterTime = System.nanoTime();
                state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(MemoryPagingBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("MemoryPagingBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
