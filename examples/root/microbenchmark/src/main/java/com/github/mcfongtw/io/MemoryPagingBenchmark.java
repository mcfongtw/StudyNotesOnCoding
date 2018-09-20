package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
import com.google.common.io.Files;
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

public class MemoryPagingBenchmark extends AbstractIoBenchmark  {

    public static Logger LOG = LoggerFactory.getLogger(MemoryPagingBenchmark.class);

    @State(Scope.Benchmark)
    public static class MemoryPagingBenchmarkExecutionPlan extends AbstractSequentialExecutionPlan {

        private final static int MAX_NUM_FILES = 1024;

        private final static int BUFFER_SIZE = UNIT_ONE_PAGE;

        LatencyMetric ioLatencyMetric = new LatencyMetric(MemoryPagingBenchmark.class.getName());

        protected List<String> listOfFinPath = new ArrayList<>();
        protected List<String> listOfFoutPath = new ArrayList<>();

        //128, 1K, 4K, 20K
        @Param({"128", "1024", "4096", "20480"})
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

            for(int index = 0; index < MAX_NUM_FILES; index ++) {
                String finPath = String.format(tempDir.getAbsolutePath() + "/in-%d.data", index);
                String foutPath = String.format(tempDir.getAbsolutePath() + "/out-%d.data", index);
                listOfFinPath.add(finPath);
                listOfFoutPath.add(foutPath);

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
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithRawBuffer(MemoryPagingBenchmarkExecutionPlan plan) throws IOException {
        for(int index = 0; index < plan.MAX_NUM_FILES; index++) {
            String finPath = plan.listOfFinPath.get(index);
            String foutPath = plan.listOfFoutPath.get(index);
            try (
                    FileInputStream fin = new FileInputStream(finPath);
                    FileOutputStream fout = new FileOutputStream(foutPath);
            ) {
                long beforeTime = System.nanoTime();

                byte[] buffer = new byte[plan.BUFFER_SIZE];
                int numBytesRead = 0;
                while ((numBytesRead = fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, numBytesRead);
                }

                assert new File(plan.finPath).length() == new File(plan.foutPath).length();

                long afterTime = System.nanoTime();
                plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithBufferedFileStream(MemoryPagingBenchmarkExecutionPlan plan) throws IOException {
        for(int index = 0; index < plan.MAX_NUM_FILES; index++) {
            String finPath = plan.listOfFinPath.get(index);
            String foutPath = plan.listOfFoutPath.get(index);
            try (
                    BufferedInputStream fin = new BufferedInputStream(new FileInputStream(finPath), plan.BUFFER_SIZE);
                    BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(foutPath), plan.BUFFER_SIZE);
            ) {
                long beforeTime = System.nanoTime();

                int byteRead = 0;
                while ((byteRead = fin.read()) != -1) {
                    fout.write(byteRead);
                }
                fout.flush();

                assert new File(plan.finPath).length() == new File(plan.foutPath).length();

                long afterTime = System.nanoTime();
                plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithFileChannel(MemoryPagingBenchmarkExecutionPlan plan) throws IOException {
        for(int index = 0; index < plan.MAX_NUM_FILES; index++) {
            String finPath = plan.listOfFinPath.get(index);
            String foutPath = plan.listOfFoutPath.get(index);
            try (
                    FileChannel finChannel = new FileInputStream(finPath).getChannel();
                    FileChannel foutChannel = new FileOutputStream(foutPath).getChannel();
            ) {
                long beforeTime = System.nanoTime();
                int finLength = (int) finChannel.size();

                for (int bufIndex = 0; bufIndex < finLength; ) {
                    ByteBuffer buffer = ByteBuffer.allocate(plan.BUFFER_SIZE);
                    int bufLength = 0;

                    if (bufIndex + plan.BUFFER_SIZE > finLength) {
                        bufLength = finLength % plan.BUFFER_SIZE;
                    } else {
                        bufLength = plan.BUFFER_SIZE;
                    }

                    finChannel.read(buffer);

                    //switch to write mode for ByteBuffer
                    buffer.flip();
                    foutChannel.write(buffer);

                    bufIndex += bufLength;

                    LOG.debug("streamed [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, plan.BUFFER_SIZE});
                }

                assert finChannel.size() == foutChannel.size();

                long afterTime = System.nanoTime();
                plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithMmap(MemoryPagingBenchmarkExecutionPlan plan) throws IOException {
        for(int index = 0; index < plan.MAX_NUM_FILES; index++) {
            String finPath = plan.listOfFinPath.get(index);
            String foutPath = plan.listOfFoutPath.get(index);

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

                    if (bufIndex + plan.BUFFER_SIZE > finLength) {
                        bufLength = finLength % plan.BUFFER_SIZE;
                    } else {
                        bufLength = plan.BUFFER_SIZE;
                    }

                    byte buffer[] = new byte[bufLength];
                    bufIn.get(buffer, 0, bufLength);
                    bufOut.put(buffer);

                    bufIndex += plan.BUFFER_SIZE;

                    LOG.debug("mmapped [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{bufIndex, finLength, plan.BUFFER_SIZE});
                }

                assert fin.length() == fout.length();

                long afterTime = System.nanoTime();
                plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void copyWithRawBufferedRandomAccessFile(MemoryPagingBenchmarkExecutionPlan plan) throws IOException {
        for(int index = 0; index < plan.MAX_NUM_FILES; index++) {
            String finPath = plan.listOfFinPath.get(index);
            String foutPath = plan.listOfFoutPath.get(index);
            try (
                    RandomAccessFile fin = new RandomAccessFile(finPath, "r");
                    RandomAccessFile fout = new RandomAccessFile(foutPath, "rw");

            ) {
                long beforeTime = System.nanoTime();

                byte[] buffer = new byte[plan.BUFFER_SIZE];

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
                plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void zeroTransferToCopy(MemoryPagingBenchmarkExecutionPlan plan) throws Exception {
        for(int index = 0; index < plan.MAX_NUM_FILES; index++) {
            String finPath = plan.listOfFinPath.get(index);
            String foutPath = plan.listOfFoutPath.get(index);
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

                    if (toIndex + plan.BUFFER_SIZE > fromLength) {
                        bufLength = fromLength % plan.BUFFER_SIZE;
                    } else {
                        bufLength = plan.BUFFER_SIZE;
                    }


                    long returnCode = fromChannel.transferTo(toIndex, bufLength, toChannel);
                    if (returnCode >= 0) {
                        LOG.debug("transferTo [{}] / [{}] bytes w/ buffer size [{}]", new Object[]{toIndex, fromLength, plan.BUFFER_SIZE});
                    } else {
                        LOG.warn("transferTo failed! error code: [{}]", returnCode);
                    }

                    toIndex += plan.BUFFER_SIZE;
                }


                assert fromFile.length() == toFile.length();

                long afterTime = System.nanoTime();
                plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(MemoryPagingBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(10)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("MemoryPagingBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
