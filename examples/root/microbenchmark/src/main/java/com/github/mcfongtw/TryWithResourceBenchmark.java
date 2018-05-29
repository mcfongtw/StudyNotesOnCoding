package com.github.mcfongtw;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.annotations.*;
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

public class TryWithResourceBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        File tryWithResourceTempDir;

        File tryCatchFinallyTempDir;

        int count = 1;

        @Setup(Level.Trial)
        public void setUp() {
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
        public void tearDown() throws IOException {
            FileUtils.deleteDirectory(tryWithResourceTempDir);
            System.out.println("Temp dir deleted at [" + tryWithResourceTempDir.getAbsolutePath() + "]");

            FileUtils.deleteDirectory(tryCatchFinallyTempDir);
            System.out.println("Temp dir deleted at [" + tryCatchFinallyTempDir.getAbsolutePath() + "]");
        }

        @Setup(Level.Iteration)
        public void iterate() throws IOException {
            count++;
            FileUtils.touch(new File(tryWithResourceTempDir.getAbsolutePath() + "/in/" + count + ".data"));
            FileUtils.touch(new File(tryWithResourceTempDir.getAbsolutePath() + "/out/" + count + ".data"));
            FileUtils.touch(new File(tryCatchFinallyTempDir.getAbsolutePath() + "/in/" + count + ".data"));
            FileUtils.touch(new File(tryCatchFinallyTempDir.getAbsolutePath() + "/out/" + count + ".data"));
        }

    }


    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 1000, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void measureCompressionOnTryWithResource(ExecutionPlan plan) throws IOException {
        try(
                FileInputStream fin = new FileInputStream(plan.tryWithResourceTempDir.getAbsolutePath() + "/in/" + plan.count + ".data");
                FileOutputStream fout = new FileOutputStream(plan.tryWithResourceTempDir.getAbsolutePath() + "/out/" + plan.count + ".data");
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
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Measurement(iterations = 1000, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void measureCompressOnTryCatchFinally(ExecutionPlan state)
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
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
