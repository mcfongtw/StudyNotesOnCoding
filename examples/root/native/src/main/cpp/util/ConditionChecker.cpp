/*
 * JNIConditionChecker.cpp
 *
 *  Created on: Jan 27, 2014
 *      Author: Michael Fong
 */

#include <iostream>

#include "ConditionChecker.h"

jboolean jni::ConditionChecker::checkClass(JNIEnv* env, const char* clazzName) {
	if (env->ExceptionCheck()) {
		std::cerr << "Failed to locate a class {" << clazzName << "}" << std::endl;
		env->ExceptionDescribe();
		env->ExceptionClear(); /* mark the exception as "handled" */

		return JNI_FALSE;
	} else {
		return JNI_TRUE;
	}
}

jboolean jni::ConditionChecker::checkMethod(JNIEnv* env, const char* clazzName, const char* methodName) {
	if (env->ExceptionCheck()) {
		std::cerr << "Failed to locate a method [" << methodName << "] for class {" << clazzName << "}" << std::endl;
		env->ExceptionDescribe();
		env->ExceptionClear(); /* mark the exception as "handled" */

		return JNI_FALSE;
	} else {
		return JNI_TRUE;
	}
}

jboolean jni::ConditionChecker::checkField(JNIEnv* env, const char* clazzName, const char* fieldName) {
	if (env->ExceptionCheck()) {
		std::cerr << "Failed to locate a field [" << fieldName << "] for class {" << clazzName << "}" << std::endl;
		env->ExceptionDescribe();
		env->ExceptionClear(); /* mark the exception as "handled" */

		return JNI_FALSE;
	} else {
		return JNI_TRUE;
	}
}

