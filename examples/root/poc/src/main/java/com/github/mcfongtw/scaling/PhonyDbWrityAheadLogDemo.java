package com.github.mcfongtw.scaling;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class PhonyDbWrityAheadLogDemo {

    private static void doWrite1(int size) throws Exception {
        FileOutputStream fos = new FileOutputStream("/tmp/db-write-ahead-log");
        byte[] data = new byte[size];
        fos.write(data);
        fos.close();
    }

    private static void doWrite0(int size) throws Exception {
        try (OutputStream os = new FileOutputStream("/tmp/db-write-ahead-auto-closable-log")) {
            byte[] data = new byte[size];
            os.write(data);
        }
    }

    private static void flushData() throws Exception {
        doWrite1((int)(Math.random() * 4 * 1048576 + 1));
    }

    private static void workerWriter(int size) throws Exception {
        doWrite0(size);
    }

    private static void mainWriter() throws Exception {
        flushData();
    }

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        workerWriter(1024);
                    } catch (Exception e) {
                    }
                }
            }
        });
        t.start();
        while (true) {
            Thread.sleep(1000);
            mainWriter();
        }
    }
}
