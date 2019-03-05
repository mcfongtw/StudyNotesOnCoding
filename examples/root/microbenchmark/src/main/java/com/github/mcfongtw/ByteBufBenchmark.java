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

public class ByteBufBenchmark {

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
    public static class ExecutionPlan {
        @Param({"0", "1024", "10240", "65536"})
        public int byteBufSize;
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureUnpooledHeapAllocAndFree(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(UNPOOLED_HEAP_BUFS.length);
        ByteBuf oldBuf = UNPOOLED_HEAP_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        UNPOOLED_HEAP_BUFS[idx] = UNPOOLED_ALLOCATOR.heapBuffer(executionPlan.byteBufSize);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureUnpooledDirectAllocAndFree(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(UNPOOLED_DIRECT_BUFS.length);
        ByteBuf oldBuf = UNPOOLED_DIRECT_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        UNPOOLED_DIRECT_BUFS[idx] = UNPOOLED_ALLOCATOR.directBuffer(executionPlan.byteBufSize);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measurePooledHeapAllocAndFree(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(POOLED_HEAP_BUFS.length);
        ByteBuf oldBuf = POOLED_HEAP_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        POOLED_HEAP_BUFS[idx] = POOLED_ALLOCATOR.heapBuffer(executionPlan.byteBufSize);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measurePooledDirectAllocAndFree(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(POOLED_DIRECT_BUFS.length);
        ByteBuf oldBuf = POOLED_DIRECT_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        POOLED_DIRECT_BUFS[idx] = POOLED_ALLOCATOR.directBuffer(executionPlan.byteBufSize);
    }

    @Benchmark
    public void measureDefaultPooledHeapAllocAndFree(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(DEFAULT_POOLED_HEAP_BUFS.length);
        ByteBuf oldBuf = DEFAULT_POOLED_HEAP_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        DEFAULT_POOLED_HEAP_BUFS[idx] = PooledByteBufAllocator.DEFAULT.heapBuffer(executionPlan.byteBufSize);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureDefaultPooledDirectAllocAndFree(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(DEFAULT_POOLED_DIRECT_BUFS.length);
        ByteBuf oldBuf = DEFAULT_POOLED_DIRECT_BUFS[idx];
        if (oldBuf != null) {
            oldBuf.release();
        }
        DEFAULT_POOLED_DIRECT_BUFS[idx] = PooledByteBufAllocator.DEFAULT.directBuffer(executionPlan.byteBufSize);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureNioDirectBuffer(ExecutionPlan executionPlan) {
        int idx = rand.nextInt(DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS.length);
        ByteBuffer oldBuf = DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS[idx];

        DEFAULT_UNPOOLED_DIRECT_BYTE_BUFFERS[idx] = ByteBuffer.allocateDirect(executionPlan.byteBufSize);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ByteBufBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("ByteBufBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
