package com.github.mcfongtw;

import com.github.mcfongtw.jni.utils.MallocArenaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class MallocArenaDemo {

    private static final int ONE_MB = 1024 * 1024; //1 mb

    private static final Logger logger = LoggerFactory.getLogger(MallocArenaDemo.class);

    public static final int SLEEP_TIME_IN_MILLIS = 60 * 1000 * 10;

    public static void main(String[] args) throws Exception {

        if(args.length < 2) {
            System.err.println("java MallocArenaDemo <Thread #> <data type> (bufferSize)");
            return;
        }

        int numOfThreads = Integer.valueOf(args[0]);
        int id = Integer.valueOf(args[1]);
        int bufferSize = args.length < 3? 10 * ONE_MB : Integer.valueOf(args[2]) * ONE_MB;

        Thread[] threads = new Thread[numOfThreads];

        for(int i = 0; i < numOfThreads; i++) {
            switch (id) {
                case 1:
                    threads[i] = new ThreadWithByteArray(bufferSize);
                    break;
                case 2:
                    threads[i] = new ThreadLocalByteBuffer(true, bufferSize);
                    break;
                case 3:
                    threads[i] = new ThreadLocalByteBuffer(false, bufferSize);
                    break;
                default:
                    break;
            }
            threads[i].start();
        }


        for(int i = 0; i < numOfThreads; i++) {
            threads[i].join();
        }
    }
}


class ThreadWithByteArray extends Thread {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private byte[] buffer;

    public ThreadWithByteArray(int bufferCapacity) {
        buffer = new byte[bufferCapacity];
        logger.info("Thread [{}] has written [{}] bytes to local array ", this.getName(), bufferCapacity);
    }

    @Override
    public void run() {
        super.run();
        MallocArenaUtils.mallocStats();
        try {
            Thread.sleep(MallocArenaDemo.SLEEP_TIME_IN_MILLIS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}

class ThreadLocalByteBuffer extends Thread {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean isHeap = false;

    private int capacity = 0;

    private ThreadLocal<ByteBuffer> threadLocal = new ThreadLocal<>();

    public ThreadLocalByteBuffer(boolean isHeap, int bufferCapacity) {
        this.isHeap = isHeap;
        this.capacity = bufferCapacity;
    }

    @Override
    public void run() {
        super.run();

        if(isHeap) {
            threadLocal.set(ByteBuffer.allocate(capacity));

        } else {
            threadLocal.set(ByteBuffer.allocateDirect(capacity));
        }

        ByteBuffer buffer = threadLocal.get();
        buffer.put((byte) capacity);

        logger.info("Thread [{}] has written [{}] bytes to local buffer", this.getName(), capacity);
        MallocArenaUtils.mallocStats();

        try {
            Thread.sleep(MallocArenaDemo.SLEEP_TIME_IN_MILLIS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}


