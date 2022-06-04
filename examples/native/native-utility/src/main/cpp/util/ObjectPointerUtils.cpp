/*
 * ObjectPointerUtils.cpp
 *
 *  Created on: Jan 28, 2014
 *      Author: Michael Fong
 */

#include "ObjectPointerUtils.h"

jfieldID jni::ObjectPointerUtils::getPointerField(JNIEnv *env, jobject obj) {
	jclass clazz = env->GetObjectClass(obj);

	// J is the type signature for long:
	return env->GetFieldID(clazz, "nativePtr", "J");
}

template<typename T>
T* jni::ObjectPointerUtils::getPointer(JNIEnv *env, jobject obj) {
	jlong ptr = env->GetLongField(obj, getPointerField(env, obj));

	return reinterpret_cast<T *>(ptr);
}

template<typename T>
void jni::ObjectPointerUtils::setPointer(JNIEnv *env, jobject obj, T *t) {
	jlong ptr = reinterpret_cast<jlong>(t);

	env->SetLongField(obj, getPointerField(env, obj), ptr);
}
