#include <sys/mman.h>
#include "NativeSystemCaller.h"
#include "ExceptionUtils.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_NativeSystemCaller_mlockall
  (JNIEnv *env, jclass clazz, jint jflags) {

    int result = mlockall((int) jflags);

    if(result < 0) {
        jni::ExceptionUtils::throwInternalErrorFromErrnoString(env);
    }

    return (jint) result;
  }


JNIEXPORT jint JNICALL Java_com_github_mcfongtw_jni_utils_NativeSystemCaller_munlockall
  (JNIEnv * env, jclass clazz) {

  int result = munlockall();

  if(result < 0) {
      jni::ExceptionUtils::throwInternalErrorFromErrnoString(env);
  }

  return (jint) result;
  }


#ifdef __cplusplus
}
#endif