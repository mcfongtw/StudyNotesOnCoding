package com.github.mcfongtw.jni.utils;

public class MallocUtils {

    static {
        JniUtils.loadLibrary("libnative-utility.so");
        JniUtils.loadLibrary("libnative-jni.so");
    }

    private MallocUtils() {
        // avoid instantiation
    }


    //javah -o ../../../native/src/main/cpp/MallocUtils.h com.github.mcfongtw.jni.utils.MallocUtils
    public static native void mallocStats();

    public static native int mallopt(int param, int value);

    public static native void mtrace();

    public static native void muntrace();

    /* mallopt options that actually do something */
    /*
            #define M_TRIM_THRESHOLD    -1
            #define M_TOP_PAD           -2
            #define M_MMAP_THRESHOLD    -3
            #define M_MMAP_MAX          -4
            #define M_CHECK_ACTION      -5
            #define M_PERTURB	    -6
            #define M_ARENA_TEST	    -7
            #define M_ARENA_MAX	    -8
    */
    public enum MallOpt{
        M_TRIM_THRESHOLD(-1),
        M_TOP_PAD(-2),
        M_MMAP_THRESHOLD(-3),
        M_MMAP_MAX(-4),
        M_CHECK_ACTION(-5),
        M_PERTURB(-6),
        M_ARENA_TEST(-7),
        M_ARENA_MAX(-8);

        private int opt;

        private MallOpt(int val) {
            opt = val;
        }

        public int getOpt() {
            return opt;
        }
    }
}
