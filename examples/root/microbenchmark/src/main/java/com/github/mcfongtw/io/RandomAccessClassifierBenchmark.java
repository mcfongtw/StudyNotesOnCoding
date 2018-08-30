package com.github.mcfongtw.io;

import com.github.mcfongtw.metrics.LatencyMetric;
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

import static com.github.mcfongtw.io.AbstractIoBenchmark.AbstractRandomAccessExecutionPlan.DataType.COUNT;

public class RandomAccessClassifierBenchmark extends AbstractIoBenchmark {

    public static Logger LOG = LoggerFactory.getLogger(RandomAccessClassifierBenchmark.class);

    @State(Scope.Benchmark)
    public static class RandomAccessClassifierExecutionPlan extends AbstractRandomAccessExecutionPlan {

        LatencyMetric ioLatencyMetric = new LatencyMetric(RandomAccessClassifierExecutionPlan.class.getName());

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
    @Measurement(iterations = NUM_ITERATION, time = 800, timeUnit = TimeUnit.MILLISECONDS)
    public void classifyWithMmap(RandomAccessClassifierExecutionPlan plan) throws IOException {
        try (
                RandomAccessFile fin = new RandomAccessFile(plan.finPath, "r");
                BufferedReader fmeta = new BufferedReader(new FileReader(plan.fmetaPath));
                BufferedReader fsummary = new BufferedReader(new FileReader(plan.fsummaryPath));

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
            for(int i = 0; i < plan.listOfFoutPath.size(); i++) {
                String fout = plan.listOfFoutPath.get(i);
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
                AbstractRandomAccessExecutionPlan.DataType dataType = AbstractRandomAccessExecutionPlan.DataType.getDataTypeByTypeId(Integer.valueOf(parts[1]));
                int index = Integer.valueOf(parts[2]);
                int length = Integer.valueOf(parts[3]);

                LOG.debug("path: [{}] | datatype: [{}] | index: [{}] | length: [{}]", new Object[]{fpath, dataType.name(), index, length});

                byte[] data = new byte[length];
                bufIn.get(data, 0, length);
                MappedByteBuffer bufOut = listOfMappedOutputBuffer.get(dataType.ordinal());
                bufOut.put(data);
            }

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);



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
    @BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = NUM_ITERATION, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void classifyWithFileStream(RandomAccessClassifierExecutionPlan plan) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(plan.finPath);
                BufferedReader fmeta = new BufferedReader(new FileReader(plan.fmetaPath));
                BufferedReader fsummary = new BufferedReader(new FileReader(plan.fsummaryPath));
        ) {

            int foutFileSize[] = new int[COUNT];
            for(int i = 0; i < foutFileSize.length; i++) {
                foutFileSize[i] = Integer.valueOf(fsummary.readLine());
            }


            List<FileOutputStream> listOfFileOutputStream = new ArrayList<>();
            for(int i = 0; i < plan.listOfFoutPath.size(); i++) {
                String fout = plan.listOfFoutPath.get(i);
                FileOutputStream fos = new FileOutputStream(fout);
                listOfFileOutputStream.add(fos);
            }


            long beforeTime = System.nanoTime();

            String line = null;
            while( (line = fmeta.readLine()) != null) {
                String parts[] = line.split(",");
                String fpath = parts[0];
                AbstractRandomAccessExecutionPlan.DataType dataType = AbstractRandomAccessExecutionPlan.DataType.getDataTypeByTypeId(Integer.valueOf(parts[1]));
                int index = Integer.valueOf(parts[2]);
                int length = Integer.valueOf(parts[3]);

                LOG.debug("path: [{}] | datatype: [{}] | index: [{}] | length: [{}]", new Object[]{fpath, dataType.name(), index, length});

                byte[] data = new byte[length];
                fis.read(data, 0, length);
                FileOutputStream fos = listOfFileOutputStream.get(dataType.ordinal());
                fos.write(data);
            }

            long afterTime = System.nanoTime();
            plan.ioLatencyMetric.addTime(afterTime - beforeTime, TimeUnit.NANOSECONDS);



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
                .warmupIterations(0)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result("RandomAccessClassifierBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }

}
