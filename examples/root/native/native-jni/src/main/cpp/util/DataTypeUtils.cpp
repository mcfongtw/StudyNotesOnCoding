/*
 * DataConverter.cpp
 *
 *  Created on: Jan 28, 2014
 *      Author: Michael Fong
 */

#include <iostream>

#include <stdlib.h>
#include <string.h>

#include "ExceptionUtils.h"
#include "DataTypeUtils.h"
#include "ConditionChecker.h"

/*
 * ***************************
 * JNI String
 * **************************
 */
std::string jni::DataTypeUtils::getNativeString(JNIEnv* env, jstring j_str) {
	return jni::DataTypeUtils::getNativeStringWithEncoding(env, j_str, "UTF-8");
}

std::string jni::DataTypeUtils::getNativeStringWithEncoding(JNIEnv* env, jstring j_str,
		const char* native_encoding) {

	//Retrieve Class "String"
	jclass clsstring = env->FindClass("java/lang/String");
	if (ConditionChecker::checkClass(env,  "java/lang/String") == JNI_FALSE) {
		return NULL;
	}

	//Retrieve Method "String.getBytes(String)"
	jmethodID mid = env->GetMethodID(clsstring, "getBytes",
			"(Ljava/lang/String;)[B");
	if (ConditionChecker::checkMethod(env,  "java/lang/String",
			"getBytes(Ljava/lang/String;)[B") == JNI_FALSE) {
		return NULL;
	}

	jstring j_str_encoding = getJavaString(env, native_encoding);

	//TODO: call getJavaByteArray() instead

	jbyteArray jByteArray = (jbyteArray) env->CallObjectMethod(j_str,
			mid, j_str_encoding);


	ExceptionUtils::catchException(env, true);

	jsize length = env->GetArrayLength(jByteArray);
	jbyte* ba = env->GetByteArrayElements(jByteArray, 0);

	char* native_chars = NULL;
	//allow empty java std::string to convert to native emtpy std::string.
	if (length >= 0) {
		native_chars = (char*) malloc(length + 1); //new   char[alen+1];
		memcpy(native_chars, ba, length);
		native_chars[length] = 0;
	}
	env->ReleaseByteArrayElements(jByteArray, ba, 0);

	std::string result = std::string(native_chars);
	delete native_chars;

	return result;
}

jstring jni::DataTypeUtils::getJavaStringWithEncoding(JNIEnv* env, const char* native_chars,
		const char* native_chars_encoding) {

	//Retrieve Class "String"
	jclass strClass = env->FindClass("java/lang/String");
	if (ConditionChecker::checkClass(env,  "java/lang/String") == JNI_FALSE) {
		return NULL;
	}

	//Retrieve Method "String.String(byte[])"
	jmethodID ctorID = env->GetMethodID(strClass, "<init>",
			"([BLjava/lang/String;)V");
	if (ConditionChecker::checkMethod(env,  "java/lang/String",
			"<init>([BLjava/lang/String;)V") == JNI_FALSE) {
		return NULL;
	}

	//Retrieve Object byte[]
	jbyteArray bytes = env->NewByteArray(strlen(native_chars));
	//fill the Java byte array with given char sequence (casted to jbyte*)
	env->SetByteArrayRegion(bytes, 0, strlen(native_chars),
			(jbyte*) native_chars);

	jstring j_str_encoding = env->NewStringUTF(native_chars_encoding);

	//Create a Java String Object
	jstring result = (jstring) env->NewObject(strClass, ctorID, bytes,
			j_str_encoding);

	//delete local reference to String class
	env->DeleteLocalRef(strClass);

	return result;
}

jstring jni::DataTypeUtils::getJavaString(JNIEnv* env, const char* native_chars) {
	return getJavaStringWithEncoding(env, native_chars, "UTF-8");
}

/*
 * ***********************
 * 1 dimensional array
 * ***********************
 */

/*
 * ***************************
 * JNI byte[]
 * **************************
 */
void jni::DataTypeUtils::getNativeByteArray(JNIEnv* env, jbyteArray jByteArray,
		std::vector<char>& cChars) {
	int length = env->GetArrayLength(jByteArray);
	jbyte* elements = env->GetByteArrayElements(jByteArray, 0);

	for (int i = 0; i < length; i++) {
		jbyte jByte = elements[i];
		cChars.push_back(jByte);
	}

	env->ReleaseByteArrayElements(jByteArray, elements, 0);
}

jbyteArray jni::DataTypeUtils::getJavaByteArray(JNIEnv* env, std::vector<char> cChars) {

	int size = cChars.size();
	jbyteArray jByteArray = env->NewByteArray(size);

	if (jByteArray == NULL) {
		ExceptionUtils::throwOutOfMemoryError(env,
				"getJavaByteArray:NewByteArray has failed");
		return NULL;
	}

	//fill a temp structure to use to poopulate the java int array
	jbyte temp[size];
	for (int i = size - 1; i >= 0; i--) {
		temp[i] = cChars.back();
		cChars.pop_back();
	}

	env->SetByteArrayRegion(jByteArray, 0, size, temp);

	return jByteArray;
}

/*
 * ***************************
 * JNI short[]
 * **************************
 */
void jni::DataTypeUtils::getNativeShortArray(JNIEnv* env, jshortArray jShortArray,
		std::vector<short>& cShorts) {
	int length = env->GetArrayLength(jShortArray);
	jshort* elements = env->GetShortArrayElements(jShortArray, 0);

	for (int i = 0; i < length; i++) {
		jshort jShort = elements[i];
		cShorts.push_back(jShort);
	}

	env->ReleaseShortArrayElements(jShortArray, elements, 0);
}

jshortArray jni::DataTypeUtils::getJavaShortArray(JNIEnv* env, std::vector<short> cShorts) {

	int size = cShorts.size();
	jshortArray jShortArray = env->NewShortArray(size);

	if (jShortArray == NULL) {
		ExceptionUtils::throwOutOfMemoryError(env,
				"getJavaShortArray:NewShortArray has failed");
		return NULL;
	}

	//fill a temp structure to use to poopulate the java int array
	jshort temp[size];
	for (int i = size - 1; i >= 0; i--) {
		temp[i] = cShorts.back();
		cShorts.pop_back();
	}

	env->SetShortArrayRegion(jShortArray, 0, size, temp);

	return jShortArray;
}

/*
 * ***************************
 * JNI int[]
 * **************************
 */
void jni::DataTypeUtils::getNativeIntegerArray(JNIEnv* env, jintArray jIntArray,
		std::vector<int>& cInts) {
	int length = env->GetArrayLength(jIntArray);
	jint* elements = env->GetIntArrayElements(jIntArray, 0);

	for (int i = 0; i < length; i++) {
		jint jInt = elements[i];
		cInts.push_back(jInt);
	}

	env->ReleaseIntArrayElements(jIntArray, elements, 0);
}

jintArray jni::DataTypeUtils::getJavaIntegerArray(JNIEnv* env, std::vector<int> cInts) {

	int size = cInts.size();
	jintArray jIntArray = env->NewIntArray(size);

	if (jIntArray == NULL) {
		ExceptionUtils::throwOutOfMemoryError(env,
				"getJavaIntArray:NewIntArray has failed");
		return NULL;
	}

	//fill a temp structure to use to poopulate the java int array
	jint temp[size];
	for (int i = size - 1; i >= 0; i--) {
		temp[i] = cInts.back();
		cInts.pop_back();
	}

	env->SetIntArrayRegion(jIntArray, 0, size, temp);

	return jIntArray;
}

/*
 * ***************************
 * JNI long[]
 * **************************
 */
void jni::DataTypeUtils::getNativeLongArray(JNIEnv* env, jlongArray jLongArray,
		std::vector<long>& cLongs) {
	int length = env->GetArrayLength(jLongArray);
	jlong* elements = env->GetLongArrayElements(jLongArray, 0);

	for (int i = 0; i < length; i++) {
		jlong jLong = elements[i];
		cLongs.push_back(jLong);
	}

	env->ReleaseLongArrayElements(jLongArray, elements, 0);
}

jlongArray jni::DataTypeUtils::getJavaLongArray(JNIEnv* env, std::vector<long> cLongs) {

	int size = cLongs.size();
	jlongArray jLongArray = env->NewLongArray(size);

	if (jLongArray == NULL) {
		ExceptionUtils::throwOutOfMemoryError(env,
				"getJavaLongArray:NewLongArray has failed");
		return NULL;
	}

	//fill a temp structure to use to poopulate the java int array
	jlong temp[size];
	for (int i = size - 1; i >= 0; i--) {
		temp[i] = cLongs.back();
		cLongs.pop_back();
	}

	env->SetLongArrayRegion(jLongArray, 0, size, temp);

	return jLongArray;
}

/*
 * ***************************
 * JNI float[]
 * **************************
 */
void jni::DataTypeUtils::getNativeFloatArray(JNIEnv* env, jfloatArray jFloatArray,
		std::vector<float>& cFloats) {
	int length = env->GetArrayLength(jFloatArray);
	jfloat* elements = env->GetFloatArrayElements(jFloatArray, 0);

	for (int i = 0; i < length; i++) {
		jfloat jFloat = elements[i];
		cFloats.push_back(jFloat);
	}

	env->ReleaseFloatArrayElements(jFloatArray, elements, 0);
}

jfloatArray jni::DataTypeUtils::getJavaFloatArray(JNIEnv* env, std::vector<float> cFloats) {

	int size = cFloats.size();
	jfloatArray jFloatArray = env->NewFloatArray(size);

	if (jFloatArray == NULL) {
		ExceptionUtils::throwOutOfMemoryError(env,
				"getJavaFloatArray:NewFloatArray has failed");
		return NULL;
	}

	//fill a temp structure to use to poopulate the java int array
	jfloat temp[size];
	for (int i = size - 1; i >= 0; i--) {
		temp[i] = cFloats.back();
		cFloats.pop_back();
	}

	env->SetFloatArrayRegion(jFloatArray, 0, size, temp);

	return jFloatArray;
}

/*
 * ***************************
 * JNI double[]
 * **************************
 */
void jni::DataTypeUtils::getNativeDoubleArray(JNIEnv* env, jdoubleArray jDoubleArray,
		std::vector<double>& cDoubles) {
	int length = env->GetArrayLength(jDoubleArray);
	jdouble* elements = env->GetDoubleArrayElements(jDoubleArray, 0);

	for (int i = 0; i < length; i++) {
		jdouble jDouble = elements[i];
		cDoubles.push_back(jDouble);
	}

	env->ReleaseDoubleArrayElements(jDoubleArray, elements, 0);
}

jdoubleArray jni::DataTypeUtils::getJavaDoubleArray(JNIEnv* env, std::vector<double> cDoubles) {
	int size = cDoubles.size();
	jdoubleArray jDoubleArray = env->NewDoubleArray(size);

	if (jDoubleArray == NULL) {
		ExceptionUtils::throwOutOfMemoryError(env,
				"getJavaDoubleArray:NewDoubleArray has failed");
		return NULL;
	}

	//fill a temp structure to use to poopulate the java double array
	jdouble temp[size];
	for (int i = size - 1; i >= 0; i--) {
		temp[i] = cDoubles.back();
		cDoubles.pop_back();
	}

	env->SetDoubleArrayRegion(jDoubleArray, 0, size, temp);

	return jDoubleArray;
}

/*
 * ***************************
 * JNI std::string[]
 * **************************
 */
void jni::DataTypeUtils::getNativeStringArrayWithEncoding(JNIEnv* env, jobjectArray jStringArray,
		std::vector<std::string>& cStrings, const char* native_encoding) {
	int length = env->GetArrayLength(jStringArray);
	for (int i = 0; i < length; i++) {
		jstring jString = (jstring) env->GetObjectArrayElement(
				jStringArray, i);
		std::string cString = getNativeStringWithEncoding(env, jString, native_encoding);
		cStrings.push_back(cString);
	}
}

void jni::DataTypeUtils::getNativeStringArray(JNIEnv* env, jobjectArray jStringArray,
		std::vector<std::string>& cStrings) {
	return getNativeStringArrayWithEncoding(env, jStringArray, cStrings, "UTF-8");
}

jobjectArray jni::DataTypeUtils::getJavaStringArrayWithEncoding(JNIEnv* env,
		std::vector<std::string> cStrings, const char* native_encoding) {

	jclass jClassString = env->FindClass("Ljava/lang/String;");
	if (ConditionChecker::checkClass(env,  "java/lang/String") == JNI_FALSE) {
		return NULL;
	}

	jobjectArray jStringArray = env->NewObjectArray(cStrings.size(),
			jClassString, NULL);
	int size = cStrings.size();

	for (int i = size - 1; i >= 0; i--) {
		jstring jstr = getJavaStringWithEncoding(env, cStrings.back().c_str(),
				native_encoding);
		cStrings.pop_back();
		env->SetObjectArrayElement(jStringArray, i, jstr);
	}

	//delete local reference to String class
	env->DeleteLocalRef(jClassString);

	return jStringArray;
}

jobjectArray jni::DataTypeUtils::getJavaStringArray(JNIEnv* env, std::vector<std::string> cStrings) {
	return getJavaStringArrayWithEncoding(env, cStrings, "UTF-8");
}

/*
 **********************
 * 2 dimensional
 ********************
 */
/*
 * ***************************
 * JNI byte[][]
 * **************************
 */
void jni::DataTypeUtils::getNative2DByteArray(JNIEnv* env, jobjectArray j_array,
		std::vector<std::vector<char> >& twoDim) {
	int outer_length = env->GetArrayLength(j_array);

	for (int i = 0; i < outer_length; i++) {
		jbyteArray j_temp = (jbyteArray) env->GetObjectArrayElement(
				j_array, i);

		std::vector<char> oneDim;
		jni::DataTypeUtils::getNativeByteArray(env, j_temp, oneDim);
		twoDim.push_back(oneDim);
	}
}

jobjectArray jni::DataTypeUtils::getJava2DByteArray(JNIEnv* env, std::vector<std::vector<char> > cChars) {

	jclass jClass = env->FindClass("[B");
	if (ConditionChecker::checkClass(env,  "[B") == JNI_FALSE) {
		return NULL;
	}

	jobjectArray j_result = env->NewObjectArray(cChars.size(), jClass,
			NULL);

	for (unsigned int i = 0; i < cChars.size(); i++) {
		jbyteArray j_array = jni::DataTypeUtils::getJavaByteArray(env, cChars[i]);

		env->SetObjectArrayElement(j_result, i, j_array);

	}

	//Delete local reference to byte[] class
	env->DeleteLocalRef(jClass);

	return j_result;
}
