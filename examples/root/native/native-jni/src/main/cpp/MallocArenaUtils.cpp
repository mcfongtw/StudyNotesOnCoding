#include <malloc.h>
#include "MallocArenaUtils.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_github_mcfongtw_jni_utils_MallocArenaUtils_mallocStats(JNIEnv *jenv, jclass jclazz) {
    malloc_stats();
}


#ifdef __cplusplus
}
#endif