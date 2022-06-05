package com.github.mcfongtw;

import java.util.*;
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
        List<? extends Map<Integer, String>> listOfMaps =
                Arrays.asList(
                        new HashMap<Integer, String>(),
                        new Hashtable<Integer, String>(),
                        new TreeMap<Integer, String>()
                        );
        for (Map<Integer, String> map : listOfMaps) {
            doBenchmark(map);
        }
    }

    private static void doBenchmark(Map<Integer, String> map) {
        final int NUM_OF_ELEMENTS = 10_000_000;
        long start = System.nanoTime();
        for (int i = 0; i < NUM_OF_ELEMENTS; i++) {
            map.put(i, String.valueOf(i));
        }
        long end = System.nanoTime();
        System.out.println(map.getClass() + " took " +  (end - start) / 1000000 + " ms");
    }
}
