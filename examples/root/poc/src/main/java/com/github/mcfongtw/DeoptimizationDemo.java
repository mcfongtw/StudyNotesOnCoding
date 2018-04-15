package com.github.mcfongtw;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

/*
 * Inspired by
 *
 * https://daniel.mitterdorfer.name/articles/2014/microbenchmarking-environment/
 *
 */
public class DeoptimizationDemo {

    public static void main(String[] args) {
        madeNotEntrantExperiment();
    }


    private static void madeNotEntrantExperiment() {
        List<? extends Set<Integer>> listOfSets =
                Arrays.asList(
                        new HashSet<Integer>(),
                        new TreeSet<Integer>(),
                        new ConcurrentSkipListSet<Integer>(),
                        Collections.synchronizedSortedSet(new TreeSet<Integer>() {
                        }));
        for (Set<Integer> set : listOfSets) {
            doBenchmark(set);
        }
    }

    private static void doBenchmark(Set<Integer> set) {
        final int NUM_OF_ELEMENTS = 10_000_000;
        long start = System.nanoTime();
        for (int i = 0; i < NUM_OF_ELEMENTS; i++) {
            set.add(i);
        }
        long end = System.nanoTime();
        System.out.println(set.getClass() + " took " +  (end - start) / 1000000 + " ms");
    }
}
