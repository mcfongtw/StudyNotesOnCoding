package com.github.mcfongtw;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Data
class IntegerEvent {
    private Integer value;

    public void clear() {
        value = null;
    }

    public final static EventFactory<IntegerEvent> EVENT_FACTORY = () -> new IntegerEvent();
}


/*
 * https://github.com/LMAX-Exchange/disruptor/wiki/Getting-Started#clearing-objects-from-the-ring-buffer
 */
class ClearEventHandler implements EventHandler<IntegerEvent> {

    @Override
    public void onEvent(IntegerEvent integerEvent, long l, boolean b) throws Exception {
        integerEvent.clear();
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
interface EventConsumer {
    /**
     * One or more event handler to handle event from ring buffer.
     */
    EventHandler<IntegerEvent>[] getEventHandlers();
}

class DuoEventConsumer implements EventConsumer {

    private int expectedValue1 = -1;

    private int expectedValue2 = -1;

    @Override
    public EventHandler<IntegerEvent>[] getEventHandlers() {
        final EventHandler<IntegerEvent> eventHandler1 = new EventHandler<IntegerEvent>() {
            @Override
            public void onEvent(IntegerEvent integerEvent, long l, boolean b) throws Exception {
                assert (++expectedValue1) == integerEvent.getValue();
            }
        };
        final EventHandler<IntegerEvent> otherEventHandler2 = new EventHandler<IntegerEvent>() {
            @Override
            public void onEvent(IntegerEvent integerEvent, long l, boolean b) throws Exception {
                assert (++expectedValue2) == integerEvent.getValue();
            }
        };
        return new EventHandler[] { eventHandler1, otherEventHandler2 };
    }
}

class SingleEventConsumer implements EventConsumer {

    private int expectedValue = -1;

    @Override
    public EventHandler<IntegerEvent>[] getEventHandlers() {
        final EventHandler<IntegerEvent> eventHandler = new EventHandler<IntegerEvent>() {
            @Override
            public void onEvent(IntegerEvent integerEvent, long l, boolean b) throws Exception {
                assert (++expectedValue) == integerEvent.getValue();
            }
        };
        return new EventHandler[] { eventHandler };
    }
}

class CleanEventConsumer implements EventConsumer {

    @Override
    public EventHandler<IntegerEvent>[] getEventHandlers() {
        return new EventHandler[] {new ClearEventHandler()};
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

interface EventProducer {
    /**
     * Start the producer that would start producing the values
     */
    void doProduce(final RingBuffer<IntegerEvent> ringBuffer, final int count);
}

class SingleEventProducer implements EventProducer{

    @Override
    public void doProduce(RingBuffer<IntegerEvent> ringBuffer, int count) {
        final Runnable producer = () -> produce(ringBuffer, count);
        new Thread(producer).start();
    }

    private void produce(final RingBuffer<IntegerEvent> ringBuffer, final int count) {
        for (int i = 0; i < count; i++) {
            final long index = ringBuffer.next();
            try {
                final IntegerEvent event = ringBuffer.get(index);
                event.setValue(i);
            } finally {
                ringBuffer.publish(index);
            }
        }
    }
}

class DuoEventProducer implements EventProducer {

    @Override
    public void doProduce(final RingBuffer<IntegerEvent> ringBuffer, final int count) {
        final Runnable simpleProducer = () -> produce(ringBuffer, count, false);
        final Runnable delayedProducer = () -> produce(ringBuffer, count, true);
        new Thread(simpleProducer).start();
        new Thread(delayedProducer).start();
    }

    private void produce(final RingBuffer<IntegerEvent> ringBuffer, final int count, final boolean isDelayNeeded) {
        for (int i = 0; i < count; i++) {
            final long index = ringBuffer.next();
            try {
                final IntegerEvent event = ringBuffer.get(index);
                event.setValue(i);
            } finally {
                ringBuffer.publish(index);
                if (isDelayNeeded) {
                    addDelay();
                }
            }
        }
    }

    private void addDelay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException interruptedException) {
            // No-Op lets swallow it
        }
    }
}

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 1)
@Warmup(iterations = 1)
@Fork(value = 1, jvmArgsAppend = {"-XX:+PrintGCDetails"})
@Threads(value = 1)
public class RingBufferBenchmark /* extends BenchmarkBase*/ {

    private static final WaitStrategy SPIN_WAIT_STRATEGY = new BusySpinWaitStrategy();

    private static final WaitStrategy BLOCKING_WAIT_STRATEGY = new BlockingWaitStrategy();

    private static final WaitStrategy SLEEPING_WAIT_STRATEGY = new SleepingWaitStrategy();

    private static final WaitStrategy TIMEDOUT_BLOCKING_WAIT_STRATEGY = new TimeoutBlockingWaitStrategy(100, TimeUnit.NANOSECONDS);

    private static final WaitStrategy YIELDING_WAIT_STRATEGY = new YieldingWaitStrategy();

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkState extends SimpleBenchmarkLifecycle {

        @Param({"16", "32"})
        public int produceCount;

        private static final int BUFFER_SIZE = 16;

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

    /////////////////////////////////////////////////////////////////////////////////
    // Single Consumer
    // Single Producer
    /////////////////////////////////////////////////////////////////////////////////

//    @Benchmark
//    public void measureSingleProducerSingleConsumer(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new SingleEventConsumer();
//        EventProducer producer = new SingleEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, BLOCKING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }

    @Benchmark
    public void measureSingleProducerSingleConsumerWithCleanEventHandler(BenchmarkState benchmarkState) {
        EventConsumer consumer = new CleanEventConsumer();
        EventProducer producer = new SingleEventProducer();

        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, BLOCKING_WAIT_STRATEGY);
        disruptor.handleEventsWith(consumer.getEventHandlers());

        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();

        producer.doProduce(ringBuffer, benchmarkState.produceCount);

        disruptor.halt();
        disruptor.shutdown();
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Multi  Consumer
    // Single Producer
    /////////////////////////////////////////////////////////////////////////////////

//    @Benchmark
//    public void measureSingleProducerMultiConsumer(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new DuoEventConsumer();
//        EventProducer producer = new SingleEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, BLOCKING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }


    /////////////////////////////////////////////////////////////////////////////////
    // Single Consumer
    // Multi  Producer
    /////////////////////////////////////////////////////////////////////////////////

//    @Benchmark
//    public void measureMultiProducerSingleConsumer(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new SingleEventConsumer();
//        EventProducer producer = new DuoEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, BLOCKING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }

    /////////////////////////////////////////////////////////////////////////////////
    // Multi Consumer
    // Multi  Producer
    // Various WaitStrategy
    /////////////////////////////////////////////////////////////////////////////////
//
//    @Benchmark
//    public void measureMultiProducerMultiConsumerWithBlockingWaitStrategy(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new DuoEventConsumer();
//        EventProducer producer = new DuoEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, BLOCKING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }
//
//    @Benchmark
//    public void measureMultiProducerMultiConsumerWithYieldingWaitStrategy(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new DuoEventConsumer();
//        EventProducer producer = new DuoEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, YIELDING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }
//
//    @Benchmark
//    public void measureMultiProducerMultiConsumerWithTimeoutBlockingWaitStrategy(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new DuoEventConsumer();
//        EventProducer producer = new DuoEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, TIMEDOUT_BLOCKING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }
//
//    @Benchmark
//    public void measureMultiProducerMultiConsumerWithSleepingWaitStrategy(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new DuoEventConsumer();
//        EventProducer producer = new DuoEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, SLEEPING_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }
//
//    @Benchmark
//    public void measureMultiProducerMultiConsumerWithSpinWaitStrategy(BenchmarkState benchmarkState) {
//        EventConsumer consumer = new DuoEventConsumer();
//        EventProducer producer = new DuoEventProducer();
//
//        Disruptor<IntegerEvent> disruptor = new Disruptor<>(IntegerEvent.EVENT_FACTORY, benchmarkState.BUFFER_SIZE, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, SPIN_WAIT_STRATEGY);
//        disruptor.handleEventsWith(consumer.getEventHandlers());
//
//        final RingBuffer<IntegerEvent> ringBuffer = disruptor.start();
//
//        producer.doProduce(ringBuffer, benchmarkState.produceCount);
//
//        disruptor.halt();
//        disruptor.shutdown();
//    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws RunnerException {
        //TODO: Need to recreate table via command line:
        Options opt = new OptionsBuilder()
                .include(RingBufferBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result("RingBufferBenchmark-result.json")
                .build();

        Collection<RunResult> runResultCollection = new Runner(opt).run();
    }
}
