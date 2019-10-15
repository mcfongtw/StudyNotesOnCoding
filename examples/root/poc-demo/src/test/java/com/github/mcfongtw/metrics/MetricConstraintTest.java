package com.github.mcfongtw.metrics;

import com.codahale.metrics.Counter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class MetricConstraintTest {


    @Test
    public void testCounterOverLimit() {
        Counter maxCounter = new Counter();
        maxCounter.inc(Long.MAX_VALUE);

        Assertions.assertEquals(Long.MAX_VALUE, maxCounter.getCount());

        maxCounter.inc(1);

        Assertions.assertEquals(Long.MAX_VALUE + 1, maxCounter.getCount());
    }

    @Test
    public void testAtomicLongOverLimit() {
        AtomicLong maxAtomicLong = new AtomicLong();
        maxAtomicLong.addAndGet(Long.MAX_VALUE);

        Assertions.assertEquals(Long.MAX_VALUE, maxAtomicLong.get());

        maxAtomicLong.addAndGet(1);

        Assertions.assertEquals(Long.MAX_VALUE + 1, maxAtomicLong.get());
    }

    @Test
    public void testLongAdderOverLimit() {
        LongAdder maxLongAdder = new LongAdder();
        maxLongAdder.add(Long.MAX_VALUE);

        Assertions.assertEquals(Long.MAX_VALUE, maxLongAdder.longValue());

        maxLongAdder.add(1);

        Assertions.assertEquals(Long.MAX_VALUE + 1, maxLongAdder.longValue());
    }
}
