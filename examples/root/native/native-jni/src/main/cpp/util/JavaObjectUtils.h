//
// Created by mcfong on 8/25/16.
//

#ifndef FFI_JNI_UTILS_JAVAOBJECTUTILS_H
#define FFI_JNI_UTILS_JAVAOBJECTUTILS_H

#include <string>
#include <vector>

#include "jnilib.h"

namespace jni {

    class JavaObjectTypeUtils {

        //TODO: Use template<type>

        static jbyte getByteField(JNIEnv *, jclass, jobject, std::string);

        static jint getIntField(JNIEnv *, jclass , jobject , std::string );

        static jlong getLongField(JNIEnv *, jclass , jobject , std::string );

        static jshort getShortField(JNIEnv *, jclass , jobject , std::string );
    };
}

#endif //FFI_JNI_UTILS_JAVAOBJECTUTILS_H
