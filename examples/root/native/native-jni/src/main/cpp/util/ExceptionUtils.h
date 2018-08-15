/*
 * ExceptionMediator.h
 *
 *  Created on: Jan 28, 2014
 *      Author: Michael Fong
 */

#ifndef EXCEPTION_UTILS_H
#define EXCEPTION_UTILS_H

#include <string>

#include "jnilib.h"

namespace jni {

class ExceptionUtils {
public:
	static jboolean catchException(JNIEnv *env, bool isPrintMessage);

	static jboolean throwOutOfMemoryError(JNIEnv *env, std::string errorMsg);

	static jboolean throwInternalError(JNIEnv *env, std::string errorMsg);

	static jboolean throwNullPointerException(JNIEnv *env,
			std::string errorMsg);

	static jboolean throwIllegalStateException(JNIEnv *env,
			std::string errorMsg);

	static jboolean throwIllegalArgumentException(JNIEnv *env,
			std::string errorMsg);

	static jboolean throwUnsupportedOperationException(JNIEnv *env,
			std::string errorMsg);

private:
	static jboolean throwExceptionByClassName(JNIEnv *env, const char *fqn,
			const char *message);

	static void printExceptionMessage(JNIEnv *env, jthrowable throwable);
};

}

#endif /* EXCEPTION_UTILS_H */
