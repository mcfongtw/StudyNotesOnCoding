package com.github.mcfongtw;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.opencsv.CSVReader;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.AbstractRowProcessor;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class CSVParsingBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        public static final String CORPUS_CSV_400K = "corpus/GeoLite2-City-Blocks-IPv6.csv";

        @Setup(Level.Trial)
        @Override
        public void doTrialSetUp() throws Exception {
            super.doTrialSetUp();
        }

        @TearDown(Level.Trial)
        @Override
        public void doTrialTearDown() throws Exception {
            super.doTrialTearDown();
        }

        @Setup(Level.Iteration)
        @Override
        public void doIterationSetup() throws Exception {
            super.doIterationSetup();
        }

        @TearDown(Level.Iteration)
        @Override
        public void doIterationTearDown() throws Exception {
            super.doIterationTearDown();
        }
    }

    @Benchmark
    public void measureWithUnivocityParser(BenchmarkState state, Blackhole blackhole) throws IOException {
        try(
                InputStream fin = getClass().getClassLoader().getResourceAsStream(state.CORPUS_CSV_400K);
        ) {
            CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setLineSeparator("\n");

            //turning off features enabled by default
            settings.getFormat().setLineSeparator("\n");
            settings.setIgnoreLeadingWhitespaces(false);
            settings.setIgnoreTrailingWhitespaces(false);
            settings.setSkipEmptyLines(false);
            settings.setColumnReorderingEnabled(false);

            settings.setProcessor(new AbstractRowProcessor() {
                @Override
                public void rowProcessed(String[] row, ParsingContext context) {
                    if(row == null){
                        return;
                    }

                    blackhole.consume(System.identityHashCode(row));
                }
            });

            com.univocity.parsers.csv.CsvParser parser = new com.univocity.parsers.csv.CsvParser(settings);
            parser.parse(fin);
        }
    }

    @Benchmark
    public void measureWithOpenCsvParser(BenchmarkState state, Blackhole blackhole) throws IOException {
        try(
                InputStream fin = getClass().getClassLoader().getResourceAsStream(state.CORPUS_CSV_400K);
                InputStreamReader reader = new InputStreamReader(fin);
                CSVReader csvReader = new CSVReader(reader);
        ) {
            String[] row = null;
            do {
                row = csvReader.readNext();
                blackhole.consume(System.identityHashCode(row));
            } while(row != null);
        }
    }

    @Benchmark
    public void measureWithJacksonCsvParser(BenchmarkState state, Blackhole blackhole) throws IOException {
        try(
                InputStream fin = getClass().getClassLoader().getResourceAsStream(state.CORPUS_CSV_400K);
        ) {
            CsvMapper csvMapper = new CsvMapper();
            csvMapper.enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.WRAP_AS_ARRAY);

            MappingIterator<String[]> iterator = csvMapper.readerFor(String[].class).readValues(fin);

            while (iterator.hasNext()) {
                String[] row = iterator.next();
                blackhole.consume(System.identityHashCode(row));
            }
        }
    }

    @Benchmark
    public void measureWithCommonCsvParser(BenchmarkState state, Blackhole blackhole) throws IOException {
        try(
                InputStream fin = getClass().getClassLoader().getResourceAsStream(state.CORPUS_CSV_400K);
                InputStreamReader reader = new InputStreamReader(fin);
        ) {
            CSVFormat format = CSVFormat.RFC4180;
            org.apache.commons.csv.CSVParser parser = new org.apache.commons.csv.CSVParser(reader, format);
            for (CSVRecord record : parser) {
                CSVRecord row = record;
                blackhole.consume(System.identityHashCode(row));
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CSVParsingBenchmark.class.getSimpleName())
                .detectJvmArgs()
                .resultFormat(ResultFormatType.JSON)
                .verbosity(VerboseMode.EXTRA)
                .result("CSVParsingBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
