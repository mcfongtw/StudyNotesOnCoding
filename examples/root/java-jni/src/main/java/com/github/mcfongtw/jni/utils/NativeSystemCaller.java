package com.github.mcfongtw.jni.utils;

public class NativeSystemCaller {

    static {
        JniUtils.loadLibrary("libnative-utility.so");
        JniUtils.loadLibrary("libnative-jni.so");
    }

    private NativeSystemCaller() {
        //avoid instantiation
    }

    public static final int MCL_CURRENT = 1;

    public static final int MCL_FUTURE = 2;

    //javah -o ../../../native/src/main/cpp/NativeSystemCaller.h com.github.mcfongtw.jni.utils.NativeSystemCaller

    public static native int mlockall(int flags);

    public static native int munlockall();

}
