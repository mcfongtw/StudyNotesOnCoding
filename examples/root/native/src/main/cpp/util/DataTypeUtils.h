/*
 * DataConverter.h
 *
 *  Created on: Jan 28, 2014
 *      Author: Michael Fong
 */

#ifndef DATATYPEUTILS_H_
#define DATATYPEUTILS_H_

#include <string>
#include <vector>

#include "jnilib.h"

namespace jni {

class DataTypeUtils {
public:
	/*
	 * ***************************
	 * JNI std::string
	 * **************************
	 */

	static std::string getNativeString(JNIEnv*, jstring);

	static std::string getNativeStringWithEncoding(JNIEnv*, jstring, const char*);

	static jstring getJavaString(JNIEnv*, const char* native_chars);

	static jstring getJavaStringWithEncoding(JNIEnv*, const char* native_chars,
			const char* native_chars_encoding);

	/*
	 * ********************
	 * 1 dimensional array
	 * ********************
	 */

	/*
	 * ***************************
	 * JNI byte[]
	 * **************************
	 */

	static void getNativeByteArray(JNIEnv*, jbyteArray jByteArray, std::vector<char>& cChars);

	static jbyteArray getJavaByteArray(JNIEnv*, std::vector<char> cChars);

	/*
	 * ***************************
	 * JNI short[]
	 * **************************
	 */

	static void getNativeShortArray(JNIEnv*, jshortArray jShortArray, std::vector<short>& cShorts);

	static jshortArray getJavaShortArray(JNIEnv*, std::vector<short> cShorts);

	/*
	 * ***************************
	 * JNI int[]
	 * **************************
	 */

	static void getNativeIntegerArray(JNIEnv*, jintArray jIntArray, std::vector<int>& cInts);

	static jintArray getJavaIntegerArray(JNIEnv*, std::vector<int> cInts);

	/*
	 * ***************************
	 * JNI long[]
	 * **************************
	 */

	static void getNativeLongArray(JNIEnv*, jlongArray jLongArray, std::vector<long>& cLongs);

	static jlongArray getJavaLongArray(JNIEnv*, std::vector<long> cLongs);

	/*
	 * ***************************
	 * JNI float[]
	 * **************************
	 */

	static void getNativeFloatArray(JNIEnv*, jfloatArray jFloatArray, std::vector<float>& cFloats);

	static jfloatArray getJavaFloatArray(JNIEnv*, std::vector<float> cFloats);

	/*
	 * ***************************
	 * JNI double[]
	 * **************************
	 */

	static void getNativeDoubleArray(JNIEnv*, jdoubleArray jDoubleArray,
			std::vector<double>& cDoubles);

	static jdoubleArray getJavaDoubleArray(JNIEnv*, std::vector<double> cDoubles);

	/*
	 * ***************************
	 * JNI std::string[]
	 * **************************
	 */
	static void getNativeStringArrayWithEncoding(JNIEnv*, jobjectArray jStringArray,
			std::vector<std::string>& cStrings, const char* native_encoding);

	static void getNativeStringArray(JNIEnv*, jobjectArray jStringArray,
			std::vector<std::string>& cStrings);

	static jobjectArray getJavaStringArrayWithEncoding(JNIEnv*, std::vector<std::string> cStrings,
			const char* native_encoding);

	static jobjectArray getJavaStringArray(JNIEnv*, std::vector<std::string> cStrings);

	/*
	 * ***************************
	 * JNI byte[][]
	 * **************************
	 */
	static void getNative2DByteArray(JNIEnv*, jobjectArray elements,
			std::vector<std::vector<char> >& cChars);

	static jobjectArray getJava2DByteArray(JNIEnv*, std::vector<std::vector<char> > cChars);
};

}
#endif /* DATATYPEUTILS_H_ */
