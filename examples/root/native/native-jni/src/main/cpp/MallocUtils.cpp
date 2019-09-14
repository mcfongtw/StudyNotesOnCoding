
#ifdef __linux__
    // There is where malloc() is defined on LINUX_OS
    #include <malloc.h>
    #include <mcheck.h>
#elif __APPLE__
    // There is where malloc() is defined on MAC_OS
    #include <stdlib.h>
#else
    #error OS Not Defined / Supported
#endif


#include <iostream>
#include "MallocUtils.h"

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_github_mcfongtw_jni_utils_MallocUtils
 * Method:    mallocStats
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_mcfongtw_jni_utils_MallocUtils_mallocStats(JNIEnv *jenv, jclass jclazz) {
    #ifdef __linux__
        malloc_stats();
    #elif __APPLE__
    #else
        #error OS Not Defined / Supported
    #endif
}


/*
 * Class:     com_github_mcfongtw_jni_utils_MallocUtils
 * Method:    mallopt
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_MallocUtils_mallopt
  (JNIEnv *jenv, jclass jclazz, jint j_param, jint j_value) {
    int c_param = (int)j_param;
    int c_value = (int)j_value;
    int result = -1;

    #ifdef __linux__
        result =  mallopt( c_param, c_value);
    #elif __APPLE__
    #else
        #error OS Not Defined / Supported
    #endif

    return result;
  }

/*
 * Class:     com_github_mcfongtw_jni_utils_MallocUtils
 * Method:    mtrace
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_mcfongtw_jni_utils_MallocUtils_mtrace
  (JNIEnv *jenv, jclass jclazz) {
    #ifdef __linux__
        mtrace();
    #elif __APPLE__
    #else
        #error OS Not Defined / Supported
    #endif
  }

/*
 * Class:     com_github_mcfongtw_jni_utils_MallocUtils
 * Method:    muntrace
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_mcfongtw_jni_utils_MallocUtils_muntrace
  (JNIEnv *jenv, jclass jclazz) {
    #ifdef __linux__
        muntrace();
    #elif __APPLE__
    #else
        #error OS Not Defined / Supported
    #endif
  }

#ifdef __cplusplus
}
#endif