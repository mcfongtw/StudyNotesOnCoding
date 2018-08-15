#include <sys/mman.h>
#include "NativeSystemCaller.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_NativeSystemCaller_mlockall
  (JNIEnv *env, jclass clazz, jint jflags) {

    int result = mlockall((int) jflags);

    return (jint) result;
  }


JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_NativeSystemCaller_munlockall
  (JNIEnv * env, jclass clazz) {

  int result = munlockall();

  return (jint) result;
  }


#ifdef __cplusplus
}
#endif