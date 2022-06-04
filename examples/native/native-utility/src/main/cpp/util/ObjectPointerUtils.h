/*
 * ObjectPointerUtils.h
 *
 *  Created on: Jan 28, 2014
 *      Author: Michael Fong
 */

#ifndef OBJECTPOINTERUTILS_H_
#define OBJECTPOINTERUTILS_H_

#include "jnilib.h"

namespace jni {

class ObjectPointerUtils {

public:
	static jfieldID getPointerField(JNIEnv *env, jobject obj);

	template<typename T>
	static T *getPointer(JNIEnv *env, jobject obj);

	template<typename T>
	static void setPointer(JNIEnv *env, jobject obj, T *t);

};

}
#endif /* OBJECTPOINTERUTILS_H_ */
