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
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(3)
@Threads(1)
public class ObjectReferenceBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        @Param({"100", "10000", "1000000"})
        public int numOfRefs;

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
    public void measureGcImpactOnWeakReference(BenchmarkState benchmarkState) {
        /*
         * A weakly referenced object will be available ONLY when the first / original referent exists.
         */
        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            Object referent = new Object();
            refs[i] = new WeakReference<Object>(referent);
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    public void measureGcImpactOnSoftReference(BenchmarkState benchmarkState) {
        /*
         * Soft Reference utilizes a LRU cache which leads to the effect that the referent is retained
         * as long as there is enough memory AND as long as it is reachable by someone.
         */
        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            Object referent = new Object();
            refs[i] = new SoftReference<Object>(referent);
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    public void measureGcImpactOnPhantomReference(BenchmarkState benchmarkState) {
        final ReferenceQueue<Object> objectReferenceQueue = new ReferenceQueue<>();

        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            Object referent = new Object();
            refs[i] = new PhantomReference<Object>(referent, objectReferenceQueue);
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    public void measureGcImpactOnStrongReference(BenchmarkState benchmarkState) {
        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = new Object();
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = null;
        }
    }


    @Benchmark
    public void measureGcImpactOnHeapBuffer(BenchmarkState benchmarkState) {
        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put((byte) 1);
            byteBuffer.flip();
            byteBuffer.get();

            refs[i] = byteBuffer;
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = null;
        }
    }

    @Benchmark
    public void measureGcImpactOnDirectBuffer(BenchmarkState benchmarkState) {
        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
            byteBuffer.put((byte) 1);
            byteBuffer.flip();
            byteBuffer.get();

            refs[i] = byteBuffer;
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
                refs[i] = null;
        }
    }

    @Benchmark
    public void measureGcImpactOnFinalizedObject(BenchmarkState benchmarkState) {
        //create
        final Object[] refs = new Object[benchmarkState.numOfRefs];
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            Object referent = new Object();
            refs[i]  = new FinalizedObject(referent);
        }

        //destroy
        for(int i = 0; i < benchmarkState.numOfRefs; i++) {
            refs[i] = null;
        }
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ObjectReferenceBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("ObjectReferenceBenchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}
