package com.github.mcfongtw;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class ByteBufBenchmark extends BenchmarkBase {

    private static final ByteBufAllocator UNPOOLED_ALLOCATOR = new UnpooledByteBufAllocator(true);
    private static final ByteBufAllocator POOLED_ALLOCATOR =
            new PooledByteBufAllocator(true, 4, 4, 8192, 11, 0, 0, 0, false); // Disable thread-local cache

    private static final int MAX_NUMBER_OF_BUFS = 8192;
    private static final Random rand = new Random();
    private static final ByteBuf[] UNPOOLED_HEAP_BUFS = new ByteBuf[MAX_NUMBER_OF_BUFS];
    private static final ByteBuf[] UNPOOLED_DIRECT_BUFS = new ByteBuf[MAX_NUMBER_OF_BUFS];
    private static final ByteBuf[] POOLED_HEAP_BUFS = new ByteBuf[MAX_NUMBER_OF_BUFS];
    private static final ByteBuf[] POOLED_DIRECT_BUFS = new ByteBuf[MAX_NUMBER_OF_BUFS];
    private static final ByteBuf[] DEFAULT_POOLED_HEAP_BUFS = new ByteBuf[MAX_NUMBER_OF_BUFS];
    private static final ByteBuf[] DEFAULT_POOLED_DIRECT_BUFS = new ByteBuf[MAX_NUMBER_OF_BUFS];

    private static final ByteBuffer[] DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS = new ByteBuffer[MAX_NUMBER_OF_BUFS];

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {
        @Param({"0", "1024", "10240", "65536"})
        public int byteBufSize;

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
    public void measureUnpooledHeapAllocAndFree(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(UNPOOLED_HEAP_BUFS.length);
        ByteBuf oldBuf = UNPOOLED_HEAP_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        UNPOOLED_HEAP_BUFS[idx] = UNPOOLED_ALLOCATOR.heapBuffer(benchmarkState.byteBufSize);
    }

    @Benchmark
    public void measureUnpooledDirectAllocAndFree(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(UNPOOLED_DIRECT_BUFS.length);
        ByteBuf oldBuf = UNPOOLED_DIRECT_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        UNPOOLED_DIRECT_BUFS[idx] = UNPOOLED_ALLOCATOR.directBuffer(benchmarkState.byteBufSize);
    }

    @Benchmark
    public void measurePooledHeapAllocAndFree(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(POOLED_HEAP_BUFS.length);
        ByteBuf oldBuf = POOLED_HEAP_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        POOLED_HEAP_BUFS[idx] = POOLED_ALLOCATOR.heapBuffer(benchmarkState.byteBufSize);
    }

    @Benchmark
    public void measurePooledDirectAllocAndFree(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(POOLED_DIRECT_BUFS.length);
        ByteBuf oldBuf = POOLED_DIRECT_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        POOLED_DIRECT_BUFS[idx] = POOLED_ALLOCATOR.directBuffer(benchmarkState.byteBufSize);
    }

    @Benchmark
    public void measureDefaultPooledHeapAllocAndFree(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(DEFAULT_POOLED_HEAP_BUFS.length);
        ByteBuf oldBuf = DEFAULT_POOLED_HEAP_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        DEFAULT_POOLED_HEAP_BUFS[idx] = PooledByteBufAllocator.DEFAULT.heapBuffer(benchmarkState.byteBufSize);
    }

    @Benchmark
    public void measureDefaultPooledDirectAllocAndFree(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(DEFAULT_POOLED_DIRECT_BUFS.length);
        ByteBuf oldBuf = DEFAULT_POOLED_DIRECT_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        DEFAULT_POOLED_DIRECT_BUFS[idx] = PooledByteBufAllocator.DEFAULT.directBuffer(benchmarkState.byteBufSize);
    }

    @Benchmark
    public void measureNioDirectBuffer(BenchmarkState benchmarkState) {
        int idx = rand.nextInt(DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS.length);
        ByteBuffer oldBuf = DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS[idx];

        DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS[idx] = ByteBuffer.allocateDirect(benchmarkState.byteBufSize);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ByteBufBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("ByteBufBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
