package com.github.mcfongtw;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class StringReplaceBenchmark {
    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "a'bc";
    private static final String SHORT_STRING_SEVERAL_MATCHES = "'a'b'c'";
    private static final String LONG_STRING_NO_MATCH =
            "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH =
            "abcabcabcabcabcabcabcabcabcabcabca'bcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_SEVERAL_MATCHES =
            "abcabca'bcabcabcabcabcabc'abcabcabca'bcabcabcabcabcabca'bcabcabcabcabcabcabc";

    private static final int NUM_ITERATION = 10;

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureJdkStringReplaceShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.replace("'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureJdkStringReplaceLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.replace("'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureJdkStringReplaceShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.replace("'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureJdkStringReplaceLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.replace("'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureJdkStringReplaceShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.replace("'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureJdkStringReplaceLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.replace("'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureApacheCommonStringUtilsReplaceShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(SHORT_STRING_NO_MATCH, "'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureApacheCommonStringUtilsReplaceLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(LONG_STRING_NO_MATCH, "'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureApacheCommonStringUtilsReplaceShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(SHORT_STRING_ONE_MATCH, "'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureApacheCommonStringUtilsReplaceLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(LONG_STRING_ONE_MATCH, "'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureApacheCommonStringUtilsReplaceShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(SHORT_STRING_SEVERAL_MATCHES, "'", "''"));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureApacheCommonStringUtilsReplaceLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(LONG_STRING_SEVERAL_MATCHES, "'", "''"));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringReplaceBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .forks(3)
                .warmupIterations(5)
                .measurementIterations(NUM_ITERATION)
                .resultFormat(ResultFormatType.JSON)
                .result("StringReplaceBenchmark-result.json")
                .build();

        Collection<RunResult> runResultCollection = new Runner(opt).run();
    }
}
