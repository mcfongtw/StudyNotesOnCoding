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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.mcfongtw.io.AbstractIoBenchmarkBase.AbstractRandomAccessIoBenchmarkLifecycle.DataType.COUNT;

public class RandomAccessClassifierBenchmark extends AbstractIoBenchmarkBase {

    public static Logger LOG = LoggerFactory.getLogger(RandomAccessClassifierBenchmark.class);

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractRandomAccessIoBenchmarkLifecycle {

        private LatencyMetric ioLatencyMetric = new LatencyMetric(RandomAccessClassifierBenchmark.class.getName());

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
    @Measurement(iterations = NUM_ITERATION, time = 800, timeUnit = TimeUnit.MILLISECONDS)
    public void classifyWithMmap(BenchmarkState state) throws IOException {
        try (
                RandomAccessFile fin = new RandomAccessFile(state.getFinPath(), "r");
                BufferedReader fmeta = new BufferedReader(new FileReader(state.getFmetaPath()));
                BufferedReader fsummary = new BufferedReader(new FileReader(state.getFsummaryPath()));

                FileChannel finChannel = fin.getChannel();
        ) {

            int foutFileSize[] = new int[COUNT];
            for(int i = 0; i < foutFileSize.length; i++) {
                foutFileSize[i] = Integer.valueOf(fsummary.readLine());
            }

            int finLength = (int) finChannel.size();

            MappedByteBuffer bufIn = finChannel.map(FileChannel.MapMode.READ_ONLY, 0, finLength);

            List<RandomAccessFile> listOfRandomAccessFile = new ArrayList<>();
            List<FileChannel> listOfFileChannel = new ArrayList<>();
            List<MappedByteBuffer> listOfMappedOutputBuffer = new ArrayList<>();
            for(int i = 0; i < state.getListOfFoutPath().size(); i++) {
                String fout = state.getListOfFoutPath().get(i);
                RandomAccessFile raf = new RandomAccessFile(fout, "rw");
                FileChannel fc = raf.getChannel();
                MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, foutFileSize[i]);
                listOfRandomAccessFile.add(raf);
                listOfFileChannel.add(fc);
                listOfMappedOutputBuffer.add(mbb);
            }

            long beforeTime = System.nanoTime();

            String line = null;
            while( (line = fmeta.readLine()) != null) {
                String parts[] = line.split(",");
                String fpath = parts[0];
                AbstractRandomAccessIoBenchmarkLifecycle.DataType dataType = AbstractRandomAccessIoBenchmarkLifecycle.DataType.getDataTypeByTypeId(Integer.valueOf(parts[1]));
                int index = Integer.valueOf(parts[2]);
                int length = Integer.valueOf(parts[3]);

                LOG.debug("path: [{}] | datatype: [{}] | index: [{}] | length: [{}]", new Object[]{fpath, dataType.name(), index, length});

                byte[] data = new byte[length * dataType.getSizeOf()];
                bufIn.get(data, 0, length);
                MappedByteBuffer bufOut = listOfMappedOutputBuffer.get(dataType.ordinal());
                bufOut.put(data);
            }

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);



            //Assert sum of ALL{fout.length} == fin.length
            int sumOfFoutLength = 0;
            for(FileChannel fc: listOfFileChannel) {
                sumOfFoutLength += fc.size();
            }
            assert fin.length() == sumOfFoutLength;

            for(FileChannel fc: listOfFileChannel) {
                fc.close();
            }
            for(RandomAccessFile raf : listOfRandomAccessFile) {
                raf.close();
            }
        }

    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void classifyWithFileStream(BenchmarkState state) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(state.getFinPath());
                BufferedReader fmeta = new BufferedReader(new FileReader(state.getFmetaPath()));
                BufferedReader fsummary = new BufferedReader(new FileReader(state.getFsummaryPath()));
        ) {

            int foutFileSize[] = new int[COUNT];
            for(int i = 0; i < foutFileSize.length; i++) {
                foutFileSize[i] = Integer.valueOf(fsummary.readLine());
            }


            List<FileOutputStream> listOfFileOutputStream = new ArrayList<>();
            for(int i = 0; i < state.getListOfFoutPath().size(); i++) {
                String fout = state.getListOfFoutPath().get(i);
                FileOutputStream fos = new FileOutputStream(fout);
                listOfFileOutputStream.add(fos);
            }


            long beforeTime = System.nanoTime();

            String line = null;
            while( (line = fmeta.readLine()) != null) {
                String parts[] = line.split(",");
                String fpath = parts[0];
                AbstractRandomAccessIoBenchmarkLifecycle.DataType dataType = AbstractRandomAccessIoBenchmarkLifecycle.DataType.getDataTypeByTypeId(Integer.valueOf(parts[1]));
                int index = Integer.valueOf(parts[2]);
                int length = Integer.valueOf(parts[3]);

                LOG.debug("path: [{}] | datatype: [{}] | index: [{}] | length: [{}]", new Object[]{fpath, dataType.name(), index, length});

                byte[] data = new byte[length * dataType.getSizeOf()];
                fis.read(data, 0, length * dataType.getSizeOf());
                FileOutputStream fos = listOfFileOutputStream.get(dataType.ordinal());
                fos.write(data);
            }

            long afterTime = System.nanoTime();
            state.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);



            //Assert sum of ALL{fout.length} == fin.length
            int sumOfFoutLength = 0;
            for(FileOutputStream fos: listOfFileOutputStream) {
                sumOfFoutLength += fos.getChannel().size();
            }
            assert fis.getChannel().size() == sumOfFoutLength;

            for(FileOutputStream fos: listOfFileOutputStream) {
                fos.close();
            }
        }

    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(RandomAccessClassifierBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .warmupIterations(10)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("RandomAccessClassifierBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }

}
