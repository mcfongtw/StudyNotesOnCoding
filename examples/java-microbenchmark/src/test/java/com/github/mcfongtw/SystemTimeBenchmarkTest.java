package com.github.mcfongtw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.function.Supplier;

public class SystemTimeBenchmarkTest {

    private static DecimalFormat df = new DecimalFormat("0.000");
    private static final double PAST_REFERENCE_SCORE = 0.045;

    @Test
    public void runJmhBenchmark() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SystemTimeBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .addProfiler(GCProfiler.class)
                .build();
        Collection<RunResult> runResults = new Runner(opt).run();
        Assertions.assertFalse(runResults.isEmpty());
        for(RunResult runResult : runResults) {
            assertDeviationWithin(runResult, 0.05);
        }
    }

    private static void assertDeviationWithin(RunResult result, double maxDeviation) {
        double score = result.getPrimaryResult().getScore();
        double deviation = Math.abs(score/PAST_REFERENCE_SCORE - 1);
        String deviationString = df.format(deviation * 100) + "%";
        String maxDeviationString = df.format(maxDeviation * 100) + "%";
        String errorMessage = "Benchmark [" + result.getPrimaryResult().getLabel()  + "] deviation " + deviationString + " exceeds maximum allowed deviation " + maxDeviationString;
        Assertions.assertTrue(deviation < maxDeviation, new Supplier<String>() {
            @Override
            public String get() {
                return errorMessage;
            }
        });
    }
}
