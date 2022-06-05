package com.github.mcfongtw;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.Data;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Data
class ObjectEvent {
    private Object object;

    public void clear() {
        object = null;
    }
}

class ObjectEventFactory implements EventFactory<ObjectEvent> {

    @Override
    public ObjectEvent newInstance() {
        return new ObjectEvent();
    }
}

/*
 * https://github.com/LMAX-Exchange/disruptor/wiki/Getting-Started#clearing-objects-from-the-ring-buffer
 */
class ClearEventHandler implements EventHandler<ObjectEvent> {

    @Override
    public void onEvent(ObjectEvent objectEvent, long l, boolean b) throws Exception {
        objectEvent.clear();
    }
}

@Data
class ObjectEventProducer {

    private final RingBuffer<ObjectEvent> ringBuffer;

    public void onData(ByteBuffer byteBuffer) {
        long index = ringBuffer.next();
        try {
            ObjectEvent objectEvent = ringBuffer.get(index);
            objectEvent.setObject(byteBuffer.asLongBuffer());
        } finally {
            ringBuffer.publish(index);
        }
    }
}

//class WeakRefedObjectEventFactory implements  EventFactory<WeakReference<ObjectEvent>> {
//
//    @Override
//    public WeakReference<ObjectEvent> newInstance() {
//        return new WeakReference<ObjectEvent>(new ObjectEvent());
//    }
//}
//
//@Data
//class WeakRefedObjectEventProducer {
//
//    private final RingBuffer<WeakReference<ObjectEvent>> ringBuffer;
//
//    public void onData(ByteBuffer byteBuffer) {
//        long index = ringBuffer.next();
//        try {
//            ObjectEvent objectEvent = ringBuffer.get(index).get();
//            objectEvent.setObject(byteBuffer.getLong(0));
//        } finally {
//            ringBuffer.publish(index);
//        }
//    }
//}

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@Fork(value = 3, jvmArgsAppend = {"-XX:+PrintGCDetails"})
@Threads(1)
public class RingBufferBenchmark extends BenchmarkBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        @Param({"1024", "10240", "102400"})
        public int size;

        private static final int BUFFER_SIZE = 1024;

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
    public void measureRingBuffer(BenchmarkState benchmarkState) {
        ObjectEventFactory objectEventFactory = new ObjectEventFactory();
        Disruptor<ObjectEvent> disruptor = new Disruptor<>(objectEventFactory, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
        disruptor.start();

        RingBuffer<ObjectEvent> ringBuffer = disruptor.getRingBuffer();
        ObjectEventProducer producer = new ObjectEventProducer(ringBuffer);

        for (long i = 0; i < benchmarkState.size; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.putLong(0, i);
            producer.onData(byteBuffer);
        }

        disruptor.shutdown();
    }

    @Benchmark
    public void measureCleanableRingBuffer(BenchmarkState benchmarkState) {
        ObjectEventFactory objectEventFactory = new ObjectEventFactory();
        Disruptor<ObjectEvent> disruptor = new Disruptor<>(objectEventFactory, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(new ClearEventHandler());
        disruptor.start();

        RingBuffer<ObjectEvent> ringBuffer = disruptor.getRingBuffer();
        ObjectEventProducer producer = new ObjectEventProducer(ringBuffer);

        for (long i = 0; i < benchmarkState.size; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.putLong(0, i);
            producer.onData(byteBuffer);
        }

        disruptor.shutdown();
    }

//    @Benchmark
//    @BenchmarkMode({Mode.AverageTime})
//    @OutputTimeUnit(TimeUnit.MICROSECONDS)
//    @Measurement(iterations = NUM_ITERATION)
//    @Warmup(iterations = 5)
//    public void measureWeaklyReferencedRingBuffer(BenchmarkState executionPlan) {
//        WeakRefedObjectEventFactory weakRefedObjectEventFactory = new WeakRefedObjectEventFactory();
//        Disruptor<WeakReference<ObjectEvent>> disruptor = new Disruptor<>(weakRefedObjectEventFactory, executionPlan.BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
//        disruptor.start();
//
//        RingBuffer<WeakReference<ObjectEvent>> ringBuffer = disruptor.getRingBuffer();
//        WeakRefedObjectEventProducer producer = new WeakRefedObjectEventProducer(ringBuffer);
//
//        for (long i = 0; i < executionPlan.size; i++) {
//            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
//            byteBuffer.putLong(0, i);
//            producer.onData(byteBuffer);
//        }
//
//        disruptor.shutdown();
//    }

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=DROP DATABASE "demo"'
        //curl -XPOST 'http://localhost:8086/query' --data-urlencode 'q=CREATE DATABASE "demo"'
        Options opt = new OptionsBuilder()
                .include(RingBufferBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("RingBufferBenchmark-result.json")
                .build();

        Collection<RunResult> runResultCollection = new Runner(opt).run();
    }
}
