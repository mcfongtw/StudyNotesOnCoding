package com.github.mcfongtw.scaling;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class PhonyDbWrityAheadLogDemo {

    private static void doWriteTraditionally(int size) throws Exception {
        FileOutputStream fos = new FileOutputStream("/tmp/db-write-ahead-0.log");
        byte[] data = new byte[size];
        fos.write(data);
        fos.close();
    }

    private static void doWriteAutoClosably(int size) throws Exception {
        try (OutputStream os = new FileOutputStream("/tmp/db-write-ahead-1.log")) {
            byte[] data = new byte[size];
            os.write(data);
        }
    }

    private static void workerWriter() throws Exception {
        doWriteTraditionally((int)(Math.random() * 4 * 1048576 + 1));
    }

    private static void mainWriter(int size) throws Exception {
        doWriteAutoClosably(size);
    }

    public static void main(String[] args) throws Exception {
        Thread diligentWorkerThread = new Thread(new Runnable() {
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
        diligentWorkerThread.start();
        while (true) {
            Thread.sleep(1000);
            mainWriter(1024);
        }
    }
}
