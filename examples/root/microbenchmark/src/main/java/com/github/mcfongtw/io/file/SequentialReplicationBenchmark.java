package com.github.mcfongtw.io.file;

import com.github.mcfongtw.io.AbstractIoBenchmarkBase;
import com.github.mcfongtw.io.InfluxdbLatencyMetric;
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
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.*;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 20)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class SequentialReplicationBenchmark extends AbstractIoBenchmarkBase {

    public static Logger LOG = LoggerFactory.getLogger(SequentialReplicationBenchmark.class);

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractSequentialIoBenchmarkLifecycle {

        InfluxdbLatencyMetric ioLatencyMetric = new InfluxdbLatencyMetric(SequentialReplicationBenchmark.class.getName());

        //32, 256, 1k, 8k, 100k
        @Param({"32", "256", "1024", "8192", "102400"})
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
        try(
            FileInputStream fin = new FileInputStream(state.getFinPath());
            FileOutputStream fout = new FileOutputStream(state.getFoutPath());
        ) {
            long beforeTime = System.nanoTime();

            byte[] buffer = new byte[state.bufferSize];
            int numBytesRead = 0;
            while ((numBytesRead = fin.read(buffer)) != -1) {
                fout.write(buffer, 0, numBytesRead);
            }

            assert new File(state.getFinPath()).length() == new File(state.getFoutPath()).length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void copyWithBufferedFileStream(BenchmarkState state) throws IOException {
        try(
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(state.getFinPath()), state.bufferSize);
            BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(state.getFoutPath()), state.bufferSize);
        ) {
            long beforeTime = System.nanoTime();

            int byteRead = 0;
            while ((byteRead = fin.read()) != -1) {
                fout.write(byteRead);
            }
            fout.flush();

            assert new File(state.getFinPath()).length() == new File(state.getFoutPath()).length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void copyWithFileChannel(BenchmarkState state) throws IOException {
        try(
            FileChannel finChannel = new FileInputStream(state.getFinPath()).getChannel();
            FileChannel foutChannel = new FileOutputStream(state.getFoutPath()).getChannel();
        ) {
            long beforeTime = System.nanoTime();
            int finLength = (int) finChannel.size();

            for (int bufIndex = 0; bufIndex < finLength; ) {
                ByteBuffer buffer = ByteBuffer.allocate(state.bufferSize);
                int bufLength = 0;

                if (bufIndex + state.bufferSize > finLength) {
                    bufLength = finLength % state.bufferSize;
                } else {
                    bufLength = state.bufferSize;
                }

                finChannel.read(buffer);

                //switch to write mode for ByteBuffer
                buffer.flip();
                foutChannel.write(buffer);

                bufIndex += bufLength;

                LOG.debug("streamed [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, state.bufferSize});
            }

            assert finChannel.size() == foutChannel.size();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void copyWithAsyncFileChannel(BenchmarkState state) throws IOException, InterruptedException, ExecutionException {
        try(
                AsynchronousFileChannel finChannel = AsynchronousFileChannel.open(Paths.get(state.getFinPath()), StandardOpenOption.READ);
                AsynchronousFileChannel foutChannel = AsynchronousFileChannel.open(Paths.get(state.getFoutPath()), StandardOpenOption.WRITE);
        ) {
            long beforeTime = System.nanoTime();
            int finLength = (int) finChannel.size();
            Semaphore semaphore = new Semaphore(1);

            for (int bufIndex = 0; bufIndex < finLength; ) {
                ByteBuffer buffer = ByteBuffer.allocate(state.bufferSize);
                int bufLength = 0;

                if (bufIndex + state.bufferSize > finLength) {
                    bufLength = finLength % state.bufferSize;
                } else {
                    bufLength = state.bufferSize;
                }


                finChannel.read(buffer, 0).get();

                final int position = bufIndex;

                //switch to write mode for ByteBuffer
                buffer.flip();
                foutChannel.write(buffer, bufIndex, semaphore, new CompletionHandler<Integer, Semaphore>() {

                    @Override
                    public void completed(Integer result, Semaphore lock) {
                        LOG.debug("streamed [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{position, finLength, state.bufferSize});

                        lock.release();
                    }

                    @Override
                    public void failed(Throwable exc, Semaphore lock) {
                        LOG.error("[failed] Failed to write: ", exc);
                    }
                });

                bufIndex += bufLength;

                semaphore.acquire();

            }


            assert finChannel.size() == foutChannel.size();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void copyWithMmap(BenchmarkState state) throws IOException {
        try (
                RandomAccessFile fin = new RandomAccessFile(state.getFinPath(), "r");
                RandomAccessFile fout = new RandomAccessFile(state.getFoutPath(), "rw");
                FileChannel finChannel = fin.getChannel();
                FileChannel foutChannel = fout.getChannel();
        ) {

            int finLength = (int) finChannel.size();

            MappedByteBuffer bufIn = finChannel.map(FileChannel.MapMode.READ_ONLY, 0, finLength);
            MappedByteBuffer bufOut = foutChannel.map(FileChannel.MapMode.READ_WRITE, 0, finLength);

            long beforeTime = System.nanoTime();

            for (int bufIndex = 0; bufIndex < finLength; ) {
                int bufLength = 0;

                if (bufIndex + state.bufferSize > finLength) {
                    bufLength = finLength % state.bufferSize;
                } else {
                    bufLength = state.bufferSize;
                }

                byte buffer[] = new byte[bufLength];
                bufIn.get(buffer, 0, bufLength);
                bufOut.put(buffer);

                bufIndex += state.bufferSize;

                LOG.debug("mmapped [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, state.bufferSize});
            }

            assert fin.length() == fout.length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }

    }

    @Benchmark
    public void copyWithRawBufferedRandomAccessFile(BenchmarkState state) throws IOException {
        try (
                RandomAccessFile fin = new RandomAccessFile(state.getFinPath(), "r");
                RandomAccessFile fout = new RandomAccessFile(state.getFoutPath(), "rw");

        ) {
            long beforeTime = System.nanoTime();

            byte[] buffer = new byte[state.bufferSize];

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

    @Benchmark
    public void zeroTransferToCopy(BenchmarkState state) throws Exception {
        try (
                RandomAccessFile fromFile = new RandomAccessFile(state.getFinPath(), "r");
                RandomAccessFile toFile = new RandomAccessFile(state.getFoutPath(), "rw");
                FileChannel fromChannel = fromFile.getChannel();
                FileChannel toChannel = toFile.getChannel();
        ) {

            int fromLength = (int) fromChannel.size();

            long beforeTime = System.nanoTime();

            for (int toIndex = 0; toIndex < fromLength; ) {
                int bufLength = 0;

                if (toIndex + state.bufferSize > fromLength) {
                    bufLength = fromLength % state.bufferSize;
                } else {
                    bufLength = state.bufferSize;
                }


                long returnCode = fromChannel.transferTo(toIndex, bufLength, toChannel);
                if(returnCode >= 0 ) {
                    LOG.debug("transferTo [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{toIndex, fromLength, state.bufferSize});
                } else {
                    LOG.warn("transferTo failed! error code: [{}]", returnCode);
                }

                toIndex += state.bufferSize;
            }


            assert fromFile.length() == toFile.length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }

    @Benchmark
    public void zeroTransferFromCopy(BenchmarkState state) throws IOException {
        try (
                RandomAccessFile fromFile = new RandomAccessFile(state.getFinPath(), "r");
                RandomAccessFile toFile = new RandomAccessFile(state.getFoutPath(), "rw");
                FileChannel fromChannel = fromFile.getChannel();
                FileChannel toChannel = toFile.getChannel();
        ) {

            int fromLength = (int) fromChannel.size();

            long beforeTime = System.nanoTime();

            for (int fromIndex = 0; fromIndex < fromLength; ) {
                int bufLength = 0;

                if (fromIndex + state.bufferSize > fromLength) {
                    bufLength = fromLength % state.bufferSize;
                } else {
                    bufLength = state.bufferSize;
                }


                long returnCode = toChannel.transferFrom(fromChannel, fromIndex, bufLength);

                if(returnCode >= 0 ) {
                    LOG.debug("transferFrom [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{fromIndex, fromLength, state.bufferSize});
                } else {
                    LOG.warn("transferFrom failed! error code: [{}]", returnCode);
                }

                fromIndex += state.bufferSize;
            }

            assert fromFile.length() == toFile.length();

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
        }
    }


    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(SequentialReplicationBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("SequentialReplicationBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
