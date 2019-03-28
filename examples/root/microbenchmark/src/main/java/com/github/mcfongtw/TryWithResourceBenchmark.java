package com.github.mcfongtw;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class TryWithResourceBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        File tryWithResourceTempDir;

        File tryCatchFinallyTempDir;

        int count = 1;

        @Setup(Level.Trial)
        @Override
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
            tryWithResourceTempDir = Files.createTempDir();
            new File(tryWithResourceTempDir.getAbsolutePath() + "/in/").mkdirs();
            new File(tryWithResourceTempDir.getAbsolutePath() + "/out/").mkdirs();
            System.out.println("Temp dir created at [" + tryWithResourceTempDir.getAbsolutePath() + "]");

            tryCatchFinallyTempDir = Files.createTempDir();
            new File(tryCatchFinallyTempDir.getAbsolutePath() + "/in/").mkdirs();
            new File(tryCatchFinallyTempDir.getAbsolutePath() + "/out/").mkdirs();
            System.out.println("Temp dir created at [" + tryCatchFinallyTempDir.getAbsolutePath() + "]");
        }

        @TearDown(Level.Trial)
        @Override
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();

            FileUtils.deleteDirectory(tryWithResourceTempDir);
            System.out.println("Temp dir deleted at [" + tryWithResourceTempDir.getAbsolutePath() + "]");

            FileUtils.deleteDirectory(tryCatchFinallyTempDir);
            System.out.println("Temp dir deleted at [" + tryCatchFinallyTempDir.getAbsolutePath() + "]");
        }

        @Setup(Level.Iteration)
        @Override
        public void doIterationSetup() throws Exception {
            super.doIterationSetup();
            count++;
            FileUtils.touch(new File(tryWithResourceTempDir.getAbsolutePath() + "/in/" + count + ".data"));
            FileUtils.touch(new File(tryWithResourceTempDir.getAbsolutePath() + "/out/" + count + ".data"));
            FileUtils.touch(new File(tryCatchFinallyTempDir.getAbsolutePath() + "/in/" + count + ".data"));
            FileUtils.touch(new File(tryCatchFinallyTempDir.getAbsolutePath() + "/out/" + count + ".data"));
        }

        @TearDown(Level.Iteration)
        @Override
        public void doIterationTearDown() throws Exception {
            super.doIterationTearDown();
        }

    }


    @Benchmark
    public void measureCompressionOnTryWithResource(BenchmarkState state) throws IOException {
        try(
                FileInputStream fin = new FileInputStream(state.tryWithResourceTempDir.getAbsolutePath() + "/in/" + state.count + ".data");
                FileOutputStream fout = new FileOutputStream(state.tryWithResourceTempDir.getAbsolutePath() + "/out/" + state.count + ".data");
                GZIPOutputStream out = new GZIPOutputStream(fout)
        ) {
            byte[] buffer = new byte[4096];
            int numBytesRead = 0;
            while ((numBytesRead = fin.read(buffer)) != -1) {
                out.write(buffer, 0, numBytesRead);
            }
        }
    }

    @Benchmark
    public void measureCompressOnTryCatchFinally(BenchmarkState state)
            throws IOException {
        FileInputStream localFileInputStream = new FileInputStream(state.tryCatchFinallyTempDir.getAbsolutePath() + "/in/" + state.count + ".data");
        Throwable localThrowable1 = null;
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(state.tryCatchFinallyTempDir.getAbsolutePath() + "/out/" + state.count + ".data");
            Throwable localThrowable2 = null;
            try {
                GZIPOutputStream localGZIPOutputStream = new GZIPOutputStream(localFileOutputStream);
                Throwable localThrowable3 = null;
                try {
                    byte[] arrayOfByte = new byte[4096];
                    int i = 0;
                    while ((i = localFileInputStream.read(arrayOfByte)) != -1) {
                        localGZIPOutputStream.write(arrayOfByte, 0, i);
                    }
                } catch (Throwable localThrowable6) {
                    localThrowable3 = localThrowable6;
                    throw localThrowable6;
                } finally {
                    if (localGZIPOutputStream != null) {
                        if (localThrowable3 != null) {
                            try {
                                localGZIPOutputStream.close();
                            } catch (Throwable localThrowable7) {
                                localThrowable3.addSuppressed(localThrowable7);
                            }
                        } else {
                            localGZIPOutputStream.close();
                        }
                    }
                }
            } catch (Throwable localThrowable4) {
                localThrowable2 = localThrowable4;
                throw localThrowable4;
            } finally {
                if (localFileOutputStream != null) {
                    if (localThrowable2 != null) {
                        try {
                            localFileOutputStream.close();
                        } catch (Throwable localThrowable8) {
                            localThrowable2.addSuppressed(localThrowable8);
                        }
                    } else {
                        localFileOutputStream.close();
                    }
                }
            }
        } catch (Throwable localThrowable2) {
            localThrowable1 = localThrowable2;
            throw localThrowable2;
        } finally {
            if (localFileInputStream != null) {
                if (localThrowable1 != null) {
                    try {
                        localFileInputStream.close();
                    } catch (Throwable localThrowable9) {
                        localThrowable1.addSuppressed(localThrowable9);
                    }
                } else {
                    localFileInputStream.close();
                }
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TryWithResourceBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("TryWithResourceBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
