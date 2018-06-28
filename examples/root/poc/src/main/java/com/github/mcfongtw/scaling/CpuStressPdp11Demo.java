package com.github.mcfongtw.scaling;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.atan;
import static java.lang.Math.cbrt;
import static java.lang.Math.tan;

public class CpuStressPdp11Demo {
    private static final int NUMBER_OF_THREADS = 16;

    public static void main(String[] args) throws Exception {
        List<Thread> threadList = new ArrayList<Thread>(NUMBER_OF_THREADS);
        
        for (int i = 1; i <= NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread(new CpuHeavyComputation());
            if (i == NUMBER_OF_THREADS) {
                // Last thread gets MAX_PRIORITY
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.setName("T-" + i + "-MAX_PRIORITY");
            } else {
                // All other threads get MIN_PRIORITY
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("T-" + i);
            }
            threadList.add(thread);
        }

        threadList.forEach(thread -> thread.start());
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}

class CpuHeavyComputation implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i < 1_000_000_000; i++) {
            double d = tan(atan(tan(atan(tan(atan(tan(atan(tan(atan(123456789.123456789))))))))));
            cbrt(d);
        }

    }
}
