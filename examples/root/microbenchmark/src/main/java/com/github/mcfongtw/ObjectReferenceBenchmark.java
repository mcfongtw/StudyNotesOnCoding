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

        @Param({"100", "10000", "1000000"})
        public int numOfRefs;
    }

    public static final int NUM_OF_ITERATIONS = 10;

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
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnWeakReference(ExecutionPlan executionPlan) {
        /*
         * A weakly referenced object will be available ONLY when the first / original referent exists.
         */
        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            Object referent = new Object();
            refs[i] = new WeakReference<Object>(referent);
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnSoftReference(ExecutionPlan executionPlan) {
        /*
         * Soft Reference utilizes a LRU cache which leads to the effect that the referent is retained
         * as long as there is enough memory AND as long as it is reachable by someone.
         */
        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            Object referent = new Object();
            refs[i] = new SoftReference<Object>(referent);
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnPhantomReference(ExecutionPlan executionPlan) {
        final ReferenceQueue<Object> objectReferenceQueue = new ReferenceQueue<>();

        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            Object referent = new Object();
            refs[i] = new PhantomReference<Object>(referent, objectReferenceQueue);
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnStrongReference(ExecutionPlan executionPlan) {
        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = new Object();
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = null;
        }
    }


    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnHeapBuffer(ExecutionPlan executionPlan) {
        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put((byte) 1);
            byteBuffer.flip();
            byteBuffer.get();

            refs[i] = byteBuffer;
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnDirectBuffer(ExecutionPlan executionPlan) {
        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            byteBuffer.put((byte) 1);
            byteBuffer.flip();
            byteBuffer.get();

            refs[i] = byteBuffer;
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
                refs[i] = null;
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @Measurement(iterations=NUM_OF_ITERATIONS)
    public void measureGcImpactOnFinalizedObject(ExecutionPlan executionPlan) {
        //create
        final Object[] refs = new Object[executionPlan.numOfRefs];
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            Object referent = new Object();
            refs[i]  = new FinalizedObject(referent);
        }

        //destroy
        for(int i = 0; i < executionPlan.numOfRefs; i++) {
            refs[i] = null;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ObjectReferenceBenchmark.class.getSimpleName())
                .forks(3)
                .warmupIterations(5)
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("ObjectReferenceBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
