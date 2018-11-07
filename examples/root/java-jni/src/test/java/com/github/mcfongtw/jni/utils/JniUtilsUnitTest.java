package com.github.mcfongtw.jni.utils;

import org.junit.jupiter.api.Test;

public class JniUtilsUnitTest {

    @Test
    public void testLoadAllExistingSharedLibrariesInOrder() throws Exception {
        JniUtils.loadLibraryFromFileSystem("libnative-utility.so");
        JniUtils.loadLibraryFromFileSystem("libnative-jni.so");
    }
}

