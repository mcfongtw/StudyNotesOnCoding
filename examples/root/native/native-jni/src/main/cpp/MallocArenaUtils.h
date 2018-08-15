/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_github_mcfongtw_jni_utils_MallocArenaUtils */

#ifndef _Included_com_github_mcfongtw_jni_utils_MallocArenaUtils
#define _Included_com_github_mcfongtw_jni_utils_MallocArenaUtils
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_github_mcfongtw_jni_utils_MallocArenaUtils
 * Method:    mallocStats
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_mcfongtw_jni_utils_MallocArenaUtils_mallocStats
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class com_github_mcfongtw_jni_utils_NativeSystemCaller */

#ifndef _Included_com_github_mcfongtw_jni_utils_NativeSystemCaller
#define _Included_com_github_mcfongtw_jni_utils_NativeSystemCaller
#ifdef __cplusplus
extern "C" {
#endif
#undef com_github_mcfongtw_jni_utils_NativeSystemCaller_MCL_CURRENT
#define com_github_mcfongtw_jni_utils_NativeSystemCaller_MCL_CURRENT 1L
#undef com_github_mcfongtw_jni_utils_NativeSystemCaller_MCL_FUTURE
#define com_github_mcfongtw_jni_utils_NativeSystemCaller_MCL_FUTURE 2L
/*
 * Class:     com_github_mcfongtw_jni_utils_NativeSystemCaller
 * Method:    mlockall
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_NativeSystemCaller_mlockall
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_github_mcfongtw_jni_utils_NativeSystemCaller
 * Method:    munlockall
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_NativeSystemCaller_munlockall
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
