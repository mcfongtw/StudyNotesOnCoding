package com.github.mcfongtw;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class ObjectReferenceBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        public final int NUM_OF_REFS = 10000;
    }

    private static class FinalizedObject {

        private Object object;

        public FinalizedObject(Object obj) {
            object = obj;
        }

        @Override
        public void finalize() {
            object = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnWeakReference(ExecutionPlan executionPlan) {
        /*
         * A weakly referenced object will be available ONLY when the first / original referent exists.
         */
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            Object referent = new Object();
            WeakReference<Object> weakReference = new WeakReference<Object>(referent);
            referent = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnSoftReference(ExecutionPlan executionPlan) {
        /*
         * Soft Reference utilizes a LRU cache which leads to the effect that the referent is retained
         * as long as there is enough memory AND as long as it is reachable by someone.
         */
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            Object referent = new Object();
            SoftReference<Object> softReference = new SoftReference<Object>(referent);
            referent = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnPhantomReference(ExecutionPlan executionPlan) {
        final ReferenceQueue<Object> objectReferenceQueue = new ReferenceQueue<>();
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            Object referent = new Object();
            PhantomReference<Object> phantomReference = new PhantomReference<Object>(referent, objectReferenceQueue);
            referent = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnHeapObject(ExecutionPlan executionPlan) {
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            Object referent = new Object();
            referent = null;
        }
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnHeapBuffer(ExecutionPlan executionPlan) {
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put((byte) 1);
            byteBuffer.flip();
            byteBuffer.get();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnDirectBuffer(ExecutionPlan executionPlan) {
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            byteBuffer.put((byte) 1);
            byteBuffer.flip();
            byteBuffer.get();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=100)
    public void measureGcImpactOnFinalizedObject(ExecutionPlan executionPlan) {
        for(int i = 0; i < executionPlan.NUM_OF_REFS; i++) {
            Object referent = new Object();
            FinalizedObject finalizedObject = new FinalizedObject(referent);
            finalizedObject = null;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ObjectReferenceBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("ObjectReferenceBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
