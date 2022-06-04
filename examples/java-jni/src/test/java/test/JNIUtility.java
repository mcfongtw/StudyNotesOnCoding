package test;

import com.github.mcfongtw.jni.Point;

public class JNIUtility {
    // //////////////////////////////////////////////////////
    // Non-array type
    // /////////////////////////////////////////////////////
    /*
     * Java <-> Native, for primitive type data
     */

    // boolean
    public static native boolean testJavaBoolean(boolean b);

    // byte - 1 byte, 8bit
    public static native byte testJavaByte(byte b);

    //
    //    // char - 2 byte, 16bit
    //    public static native char testJavaChar(char c);
    //


    // short - 2 byte, 16bit
    public static native short testJavaShort(short r);


    // integer - 4 byte, 32bit
    public static native int testJavaInteger(int i);


    // long - 8 byte, 64bit
    public static native long testJavaLong(long i);

//    // float - 4 byte, 32bit
    public static native float testJavaFloat(float f);

        // double - 8 byte, 64bit
        public static native double testJavaDouble(double d);

    // ///////////////////////////////////////////////////
    /*
     * Java <-> Native, for object type data
     */


    public static native String testJavaString(String str, String encoding);


    // Object
    public static native Point testJavaObject(Point p);

    // //////////////////////////////////////////////////////
    // 1D-array type
    // /////////////////////////////////////////////////////
    /*
     * Java <-> Native, for primitive type array data.
     */
    // byte[]
    public static native byte[] testJavaByteArray(byte[] array);

    // short[]
    public static native short[] testJavaShortArray(short[] array);

    // integer[]
    public static native int[] testJavaIntegerArray(int[] array);

    // long[]
    public static native long[] testJavaLongArray(long[] array);

    // float[]
    public static native float[] testJavaFloatArray(float[] array);

    // double[]
    public static native double[] testJavaDoubleArray(double[] array);

    // //////////////////////////////////////////////////////
    /*
     * Java <-> Native, for Object type array data
     */
    public static native String[] testJavaStringArray(String[] array, String encoding);


//    // //////////////////////////////////////////////////////
//    // 2D-array type
//    // /////////////////////////////////////////////////////
//    // Object[]
//    public static native Point[] testJavaObjectArray(Point[] array);


//    /*
//     * Java <-> Native, for primitive type array data.
//     */
//    // boolean[]
//    public static native boolean[][] testJavaBooleanArray(boolean[][] array);
//
    // byte[]
    public static native byte[][] test2DJavaByteArray(byte[][] array);


    // //////////////////////////////////////////////////////
    // Exception handling
    // /////////////////////////////////////////////////////
//
    public static native void testThrowOutOfMemoryError();


    public static native void testThrowInternalError();


    public static native void testThrowNullPointerException();


    public static native void testThrowIllegalStateException();

    public static native void testThrowIllegalArgumentException();

    public static native void testThrowUnsupportedOperationException();
}