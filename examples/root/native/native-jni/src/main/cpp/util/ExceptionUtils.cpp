/*
 * ExceptionMediator.cpp
 *
 *  Created on: Jan 28, 2014
 *      Author: Michael Fong
 */
#include <iostream>

#include "ExceptionUtils.h"
#include "DataTypeUtils.h"
#include "ConditionChecker.h"

jboolean jni::ExceptionUtils::throwOutOfMemoryError(JNIEnv *env, std::string errorMsg) {
	jboolean result = jni::ExceptionUtils::throwExceptionByClassName(env,
			"java/lang/OutOfMemoryError", errorMsg.c_str());

	if (result == JNI_FALSE) {
		std::cout << "FATAL ERROR:  OutOfMemory: Thrown failed" << std::endl;
	}

	return result;
}

/*
 *  A fatal error in a JNI call
 *  Create and throw an 'InternalError'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean jni::ExceptionUtils::throwInternalError(JNIEnv *env, std::string errorMsg) {
	jboolean result = jni::ExceptionUtils::throwExceptionByClassName(env,
			"java/lang/InternalError", errorMsg.c_str());

	if (result == JNI_FALSE) {
		std::cout << "FATAL ERROR:  JVM Internal: Thrown failed" << std::endl;
	}

	return result;
}

/*
 *
 *  Create and throw an 'NullPointerException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean jni::ExceptionUtils::throwNullPointerException(JNIEnv *env,
		std::string errorMsg) {
	jboolean result = jni::ExceptionUtils::throwExceptionByClassName(env,
			"java/lang/NullPointerException", errorMsg.c_str());

	if (result == JNI_FALSE) {
		std::cout << "FATAL ERROR:  NullPoitner: Thrown failed" << std::endl;
	}

	return result;
}

/*
 *
 *  Create and throw an 'IllegalArgumentException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean jni::ExceptionUtils::throwIllegalStateException(JNIEnv *env,
		std::string errorMsg) {
	jboolean result = jni::ExceptionUtils::throwExceptionByClassName(env,
			"java/lang/IllegalStateException", errorMsg.c_str());

	if (result == JNI_FALSE) {
		std::cout << "FATAL ERROR:  Bad State: Thrown failed" << std::endl;
	}

	return result;
}

/*
 *
 *  Create and throw an 'IllegalArgumentException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean jni::ExceptionUtils::throwIllegalArgumentException(JNIEnv *env,
		std::string errorMsg) {
	jboolean result = jni::ExceptionUtils::throwExceptionByClassName(env,
			"java/lang/IllegalArgumentException", errorMsg.c_str());

	if (result == JNI_FALSE) {
		std::cout << "FATAL ERROR:  Bad Argument: Thrown failed" << std::endl;
	}

	return result;
}

/*
 *  Some feature Not implemented yet
 *  Create and throw an 'UnsupportedOperationException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean jni::ExceptionUtils::throwUnsupportedOperationException(JNIEnv *env,
		std::string errorMsg) {
	jboolean result = jni::ExceptionUtils::throwExceptionByClassName(env,
			"java/lang/UnsupportedOperationException", errorMsg.c_str());

	if (result == JNI_FALSE) {
		std::cout << "FATAL ERROR:  Unsupported: Thrown failed" << std::endl;
	}

	return result;
}

jboolean jni::ExceptionUtils::throwExceptionByClassName(JNIEnv *env,
		const char *fqn, const char *message) {
	jclass clazz = env->FindClass(fqn);

	if (ConditionChecker::checkClass(env, fqn) == JNI_FALSE) {
		return JNI_FALSE;
	}

	if (clazz != NULL) {
		env->ThrowNew(clazz, message);
		env->DeleteLocalRef(clazz);

		return JNI_TRUE;
	}

	return JNI_FALSE;
}

jboolean jni::ExceptionUtils::catchException(JNIEnv *env, bool isPrintMessage) {
	if (env->ExceptionCheck()) {
		jthrowable throwable = env->ExceptionOccurred();

		env->ExceptionDescribe(); /* optionally dump the stack trace */
		env->ExceptionClear(); /* mark the exception as "handled" */

		if (isPrintMessage) {
			jni::ExceptionUtils::printExceptionMessage(env, throwable);
		}

		return JNI_TRUE;
	}

	return JNI_FALSE;
}

void jni::ExceptionUtils::printExceptionMessage(JNIEnv *env, jthrowable throwable) {
	jclass jThrowableClazz = env->GetObjectClass(throwable);

	//get Exception.getMessage(String)
	jmethodID jGetMessageMethod = env->GetMethodID(jThrowableClazz,
			"getMessage", "()Ljava/lang/String;");
	if (ConditionChecker::checkMethod(env, "java/lang/Exception",
			"getMessage()Ljava/lang/String;") == JNI_FALSE) {
		return;
	}

	jstring java_message = (jstring) env->CallObjectMethod(throwable,
			jGetMessageMethod);
	std::string message = DataTypeUtils::getNativeString(env, java_message);

	if (message.empty() == false) {
		std::cout << "ERROR: [" << message << "]" << std::endl;
	}
	env->DeleteLocalRef(jThrowableClazz);
}

