package com.github.mcfongtw;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FibonacciBenchmarkTest {

    @Test
    public void testFibonacciWithIndex5() {
        //1, 1, 2, 3, 5
        Assertions.assertEquals(5, FibonacciBenchmark.doIterative(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doRecursion(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doTailRecursive(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doDpTopDown(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doDpBottomUp(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doJava8Stream(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doRxStream(5));
        Assertions.assertEquals(5, FibonacciBenchmark.doReactorStream(5));
    }

    @Test
    public void testFibonacciWithIndex10() {
        //1, 1, 2, 3, 5, 8, 13, 21, 34, 55
        Assertions.assertEquals(55, FibonacciBenchmark.doIterative(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doRecursion(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doTailRecursive(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doDpTopDown(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doDpBottomUp(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doJava8Stream(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doRxStream(10));
        Assertions.assertEquals(55, FibonacciBenchmark.doReactorStream(10));
    }
}
