package com.github.mcfongtw.jni.utils;

public class MallocArenaUtils {

    static {
        JniUtils.loadLibrary("libnative-utility.so");
        JniUtils.loadLibrary("libnative-jni.so");
    }

    private MallocArenaUtils() {
        // avoid instantiation
    }


    //javah -o ../../../native/src/main/cpp/MallocArenaUtils.h com.github.mcfongtw.jni.utils.MallocArenaUtils
    public static native void mallocStats();
}
