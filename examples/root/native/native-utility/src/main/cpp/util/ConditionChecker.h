/*
 * JNIConditionChecker.h
 *
 *  Created on: Jan 27, 2014
 *      Author: Michael Fong
 */

#ifndef CONDITIONCHECKER_H_
#define CONDITIONCHECKER_H_

#include "jnilib.h"

namespace jni {

class ConditionChecker {
public:
	static jboolean checkClass(JNIEnv* env, const char* clazzName);

	static jboolean checkMethod(JNIEnv* env, const char* clazzName, const char* methodName);

	static jboolean checkField(JNIEnv* env, const char* clazzName, const char* fieldName);
};

}

#endif /* CONDITIONCHECKER_H_ */
