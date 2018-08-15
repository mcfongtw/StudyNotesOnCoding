package com.github.mcfongtw.jni;

import com.github.mcfongtw.jni.utils.JniUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.JNIUtility;

import java.io.IOException;

public class JNIUtilityTestUnit {

    @BeforeAll
    public static void init() throws IOException {
        JniUtils.loadLibraryFromFile("../poc/target/classes/libnative-utility.so");
        JniUtils.loadLibraryFromFile("../poc/target/classes/libnative-jni.so");
    }

    ////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testJavaBoolean() {
        Assertions.assertEquals(false, JNIUtility.testJavaBoolean(true));
        Assertions.assertEquals(true, JNIUtility.testJavaBoolean(false));
    }

    @Test
    public void testJavaByte() {
        Assertions.assertEquals((byte) 1, JNIUtility.testJavaByte((byte) 2));
        // -128 -1 = 127
        Assertions.assertEquals((byte) 127, JNIUtility.testJavaByte(Byte.MIN_VALUE));
        Assertions.assertEquals((byte) (Byte.MIN_VALUE - 1), JNIUtility.testJavaByte(Byte.MIN_VALUE));
        // 127 -1 = 126
        Assertions.assertEquals((byte) 126, JNIUtility.testJavaByte(Byte.MAX_VALUE));
        Assertions.assertEquals(Byte.MAX_VALUE - 1, JNIUtility.testJavaByte(Byte.MAX_VALUE));
    }

//    @Test
//    public void testJavaChar() {
//        Assertions.assertEquals('a', JNIUtility.testJavaChar('b'));
////      Assertions.assertEquals((byte) -128, JNIUtility.testJavaChar(Byte.MIN_VALUE));
////      Assertions.assertEquals((byte) 127, JNIUtility.testJavaChar(Byte.MAX_VALUE));
//    }

    @Test
    public void testJavaShort() {
        Assertions.assertEquals((short) 1, JNIUtility.testJavaShort((short) 2));
        // -32768 -1 = 32767
        Assertions.assertEquals((short) 32767, JNIUtility.testJavaShort(Short.MIN_VALUE));
        Assertions.assertEquals((short) (Short.MIN_VALUE - 1), JNIUtility.testJavaShort(Short.MIN_VALUE));
        // 32767 -1 = 32766
        Assertions.assertEquals((short) 32766, JNIUtility.testJavaShort(Short.MAX_VALUE));
        Assertions.assertEquals(Short.MAX_VALUE - 1, JNIUtility.testJavaShort(Short.MAX_VALUE));
    }

    @Test
    public void testJavaInteger() {
        Assertions.assertEquals(1, JNIUtility.testJavaInteger(2));
        // -2,147,483,648 - 1 = 2,147,483,647
        Assertions.assertEquals(2147483647, JNIUtility.testJavaInteger(Integer.MIN_VALUE));
        Assertions.assertEquals(Integer.MIN_VALUE - 1, JNIUtility.testJavaInteger(Integer.MIN_VALUE));
        // 2,147,483,647 -1 = 2,147,483,646
        Assertions.assertEquals(2147483646, JNIUtility.testJavaInteger(Integer.MAX_VALUE));
        Assertions.assertEquals(Integer.MAX_VALUE - 1, JNIUtility.testJavaInteger(Integer.MAX_VALUE));
    }

    @Test
    public void testJavaLong() {
        Assertions.assertEquals(1L, JNIUtility.testJavaLong(2L));
        // -9,223,372,036,854,775,808 - 1 = -9,223,372,036,854,775,807
        Assertions.assertEquals(9223372036854775807L, JNIUtility.testJavaLong(Long.MIN_VALUE));
        Assertions.assertEquals(Long.MIN_VALUE - 1, JNIUtility.testJavaLong(Long.MIN_VALUE));
        // 9,223,372,036,854,775,807 - 1 = 9,223,372,036,854,775,806
        Assertions.assertEquals(9223372036854775806L, JNIUtility.testJavaLong(Long.MAX_VALUE));
        Assertions.assertEquals(Long.MAX_VALUE - 1, JNIUtility.testJavaLong(Long.MAX_VALUE));
    }

    @Test
    public void testJavaFloat() {
        Assertions.assertEquals(1.23f, JNIUtility.testJavaFloat(2.23f), 0.01f);
        Assertions.assertEquals(Float.MIN_VALUE - 1, JNIUtility.testJavaFloat(Float.MIN_VALUE), 0.01f);
        Assertions.assertEquals(Float.MAX_VALUE - 1, JNIUtility.testJavaFloat(Float.MAX_VALUE), 0.01f);
    }

    @Test
    public void testJavaDouble() {
        // 2.01 - 1 = 1.00999999999, epsilon < 0.01
        Assertions.assertEquals(1.01d, JNIUtility.testJavaDouble(2.01d), 0.01d);
        Assertions.assertEquals(Double.MIN_VALUE - 1, JNIUtility.testJavaDouble(Double.MIN_VALUE), 0.01d);
        Assertions.assertEquals(Double.MAX_VALUE - 1, JNIUtility.testJavaDouble(Double.MAX_VALUE), 0.01d);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    // String
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static String testJavaString(String str) {
        return JNIUtility.testJavaString(str, DEFAULT_ENCODING);
    }

    @Test
    public void testJavaString() {
        Assertions.assertEquals("cba", testJavaString("abc"));
        Assertions.assertEquals("b a", JNIUtility.testJavaString("a b", DEFAULT_ENCODING));
        Assertions.assertEquals("a", JNIUtility.testJavaString("a", DEFAULT_ENCODING));
        Assertions.assertEquals("", JNIUtility.testJavaString("", DEFAULT_ENCODING));
        // won't work , since the string is reversed byte by byte
//        Assertions.assertEquals("二一", JNIUtility.testJavaString("一二", DEFAULT_ENCODING));
    }

    private static final Point ORIGIN_PT = new Point(0, 0);

    @Test
    public void testJavaObject() {
        Assertions.assertEquals(ORIGIN_PT, JNIUtility.testJavaObject(new Point(1, 1)));
    }

    ////////////////////////////////////////////////////////////////////////////////////
//    // boolean[]
//    private static native boolean[] testJavaBooleanArray(boolean[] array);
//
//    @Test
//    public void testJavaBooleanArray() {
//        int size = Byte.MAX_VALUE;
//        boolean[] array = new boolean[size];
//        for (int index = 0; index < size; index++) {
//            array[index] = (index % 2) == 0;
//        }
//
//        //XXX: no api for assertArrayEquals(boolean[], boolean[])
//        Assertions.assertArrayEquals(array, JNIUtility.testJavaBooleanArray(array));
//    }

    @Test
    public void testJavaByteArray() {
        int size = Byte.MAX_VALUE;
        byte[] array = new byte[size];
        for (byte b = 0; b < size; b++) {
            array[b] = b;
        }
        
        Assertions.assertArrayEquals(array, JNIUtility.testJavaByteArray(array));
    }

    @Test
    public void testJavaShortArray() {
        int size = Short.MAX_VALUE;
        short[] array = new short[size];
        for (short s = 0; s < size; s++) {
            array[s] = s;
        }

        Assertions.assertArrayEquals(array, JNIUtility.testJavaShortArray(array));
    }

    @Test
    public void testJavaIntegerArray() {
        int size = Short.MAX_VALUE;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }

        Assertions.assertArrayEquals(array, JNIUtility.testJavaIntegerArray(array));
    }

    @Test
    public void testJavaLongArray() {
        int size = Short.MAX_VALUE;
        long[] array = new long[size];
        for (int l = 0; l < size; l++) {
            array[l] = l;
        }

        Assertions.assertArrayEquals(array, JNIUtility.testJavaLongArray(array));
    }


    @Test
    public void testJavaFloatArray() {
        int size = Short.MAX_VALUE;
        float[] array = new float[size];
        for (int i = 0; i < size; i++) {
            array[i] = i * 1.01f;
        }

        Assertions.assertArrayEquals(array, JNIUtility.testJavaFloatArray(array), 0.1f);
    }


    @Test
    public void testJavaDoubleArray() {
        int size = Short.MAX_VALUE;
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = i * 1.01d;
        }

        Assertions.assertArrayEquals(array, JNIUtility.testJavaDoubleArray(array), 0.1d);
    }


    // String[]
    private static String[] testJavaStringArray(String[] array) {
        return JNIUtility.testJavaStringArray(array, DEFAULT_ENCODING);
    }


    @Test
    public void testJavaStringArray() {
        Assertions.assertArrayEquals(new String[] { "cba", "fed", "a", "" },
                testJavaStringArray(new String[] { "abc", "def", "a", "" }));
        Assertions.assertArrayEquals(new String[] { "cba", "fed", "a", "" },
                JNIUtility.testJavaStringArray(new String[] { "abc", "def", "a", "" }, DEFAULT_ENCODING));
    }


    ////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test2DJavaByteArray() {
        int size = Byte.MAX_VALUE;
        byte[][] array = new byte[2][size];
        for (int out_i = 0; out_i < 2; out_i++) {
            for (byte b = 0; b < size; b++) {
                array[out_i][b] = (out_i == 0) ? b : (byte) -b;
            }
        }

        Assertions.assertArrayEquals(array[0], JNIUtility.test2DJavaByteArray(array)[0]);
        Assertions.assertArrayEquals(array[1], JNIUtility.test2DJavaByteArray(array)[1]);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testOutOfMemoryError() {
        try {
            JNIUtility.testThrowOutOfMemoryError();
        } catch (OutOfMemoryError e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testInternalError() {
        try {
            JNIUtility.testThrowInternalError();
        } catch (InternalError e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testNullPointerException() {
        try {
            JNIUtility.testThrowNullPointerException();
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testIllegalStateException() {
        try {
            JNIUtility.testThrowIllegalStateException();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testIllegalArgumentException() {
        try {
            JNIUtility.testThrowIllegalArgumentException();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testUnsupportedOperationException() {
        try {
            JNIUtility.testThrowUnsupportedOperationException();
        } catch (UnsupportedOperationException e) {
            System.out.println(e.getMessage());
        }
    }
}
