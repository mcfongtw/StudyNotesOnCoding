//
// Created by mcfong on 8/25/16.
//

#include "JavaObjectUtils.h"
#include "ExceptionUtils.h"

jbyte jni::JavaObjectTypeUtils::getByteField(JNIEnv *env, jclass j_obj_clazz, jobject j_obj, std::string fieldName) {
    jfieldID j_obj_field = env->GetFieldID(j_obj_clazz, fieldName.c_str(), "B");
    if(j_obj_field == NULL) {
        jni::ExceptionUtils::throwInternalError(env, "Cannot resolve byte field for " + fieldName);
    }
    jbyte ret = env->GetByteField(j_obj, j_obj_field);

    return ret;
}

jint jni::JavaObjectTypeUtils::getIntField(JNIEnv *env, jclass j_obj_clazz, jobject j_obj, std::string fieldName) {
    jfieldID j_obj_field = env->GetFieldID(j_obj_clazz, fieldName.c_str(), "I");
    if(j_obj_field == NULL) {
        jni::ExceptionUtils::throwInternalError(env, "Cannot resolve int field for " + fieldName);
    }
    jint ret = env->GetIntField(j_obj, j_obj_field);

    return ret;
}

jlong jni::JavaObjectTypeUtils::getLongField(JNIEnv *env, jclass j_obj_clazz, jobject j_obj, std::string fieldName) {
    jfieldID j_obj_field = env->GetFieldID(j_obj_clazz, fieldName.c_str(), "J");
    if(j_obj_field == NULL) {
        jni::ExceptionUtils::throwInternalError(env, "Cannot resolve long field for " + fieldName);
    }
    jlong ret = env->GetLongField(j_obj, j_obj_field);

    return ret;
}

jshort jni::JavaObjectTypeUtils::getShortField(JNIEnv *env, jclass j_obj_clazz, jobject j_obj, std::string fieldName) {
    jfieldID j_obj_field = env->GetFieldID(j_obj_clazz, fieldName.c_str(), "S");
    if(j_obj_field == NULL) {
        jni::ExceptionUtils::throwInternalError(env, "Cannot resolve short field for " + fieldName);
    }
    jshort ret = env->GetShortField(j_obj, j_obj_field);

    return ret;
}