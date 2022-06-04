package com.github.mcfongtw.jni.utils;

public class NativeSystemCaller {

    static {
        JniUtils.loadLibraryFromResourceStream("libnative-jni.so");
        JniUtils.loadLibraryFromResourceStream("libnative-utility.so");
    }

    private NativeSystemCaller() {
        //avoid instantiation
    }

    /*
     * //Flags for `mlockall'
     * #define MCL_CURRENT	1		/* Lock all currently mapped pages.
     * #define MCL_FUTURE	2		/* Lock all additions to address
     */
    public static final int MCL_CURRENT = 0b01;

    public static final int MCL_FUTURE = 0b10;

    public static native int mlockall(int flags);

    public static native int munlockall();

}
