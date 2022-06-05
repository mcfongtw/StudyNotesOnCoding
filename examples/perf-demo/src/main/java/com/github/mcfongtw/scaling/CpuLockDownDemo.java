package com.github.mcfongtw.scaling;

/**
 *  Generates Load on the CPU by keeping it busy for the given load percentage
 */
public class CpuLockDownDemo {

    private static final int SEC_TO_MS = 1000;

    /**
     * Starts the Load Generation
     * @param args Command line arguments, ignored
     */
    public static void main(String[] args) {
        int numCore = 1;
        int numThreadsPerCore = 2;
        double load = 0.6; //60%
        final long duration = 3000 * SEC_TO_MS; //3000 sec`
        for (int thread = 0; thread < numCore * numThreadsPerCore; thread++) {
            new BusyThread("Thread" + thread, load, duration).start();
        }
    }

    /**
     * Thread that actually generates the given load
     * @author Sriram
     */
    private static class BusyThread extends Thread {
        private double load;
        private long duration;

        /**
         * Constructor which creates the thread
         * @param name Name of this thread
         * @param load Load % that this thread should generate
         * @param duration Duration that this thread should generate the load for
         */
        public BusyThread(String name, double load, long duration) {
            super(name);
            this.load = load;
            this.duration = duration;
        }

        /**
         * Generates the load when run
         */
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            try {
                // Loop for the given duration
                while (System.currentTimeMillis() - startTime < duration) {
                    // Every 100ms, sleep for the percentage of unladen time
                    if (System.currentTimeMillis() % 100 == 0) {
                        Thread.sleep((long) Math.floor((1 - load) * 100));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}