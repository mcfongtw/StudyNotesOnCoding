package com.github.mcfongtw.oom;

public class InsufficientHeapSpaceDemo {
    //1 int occupies 4 bytes
    public static final int SIZE = 1024 * 1024 * 1024;

    public static void main(String[] a) {
        // total 4g bytes
        // default Xmx
        // uintx MaxHeapSize                              := 4137680896
        int[] i = new int[SIZE];
    }

}
