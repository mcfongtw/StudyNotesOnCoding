package com.github.mcfongtw.scaling;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class CrazyDbWriteAheadLogDemo {

    private static void doWriteIndefinitely(int size) throws Exception {
        FileOutputStream fos = new FileOutputStream("/tmp/db-write-ahead-0.log");
        for(;;) {
            byte[] data = new byte[size];
            fos.write(data);
        }
//        fos.close();
    }

    private static void doWriteAutoClosably(int size) throws Exception {
        try (OutputStream os = new FileOutputStream("/tmp/db-write-ahead-1.log")) {
            byte[] data = new byte[size];
            os.write(data);
        }
    }

    private static void workerWriter() throws Exception {
        doWriteIndefinitely((int)(Math.random() * 4 * 1048576 + 1));
    }

    private static void mainWriter(int size) throws Exception {
        doWriteAutoClosably(size);
    }

    public static void main(String[] args) throws Exception {
        Thread crazyWorkerThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        workerWriter();
                    } catch (Exception e) {
                    }
                }
            }
        });
        crazyWorkerThread.start();
        while (true) {
            Thread.sleep(1000);
            mainWriter(1024);
        }
    }
}
