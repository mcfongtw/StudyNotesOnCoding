#include "JNIUtility.h"
#include "DataTypeUtils.h"
#include "ExceptionUtils.h"

#include <iostream>
#include <string>

using namespace std;

void reverse_string(string &str);

/*
 * Class:     test_JNIUtility
 * Method:    testJavaBoolean
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_test_JNIUtility_testJavaBoolean
  (JNIEnv *env, jclass clazz, jboolean java_val) {	
	bool native_val = !(bool) java_val;
	std::cout << "Java [" << (bool) java_val << "] - 1 = native {" << native_val  << "}" << std::endl;
	
	return (jboolean) native_val;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaByte
 * Signature: (B)B
 */
JNIEXPORT jbyte JNICALL Java_test_JNIUtility_testJavaByte
  (JNIEnv *env, jclass clazz, jbyte java_val) {
	char native_val = ((char) java_val - 1);
	std::cout << "Java [" << (int) java_val << "] - 1 = native {" << (int) native_val  << "}" << std::endl;

	return (jbyte) native_val;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaShort
 * Signature: (S)S
 */
JNIEXPORT jshort JNICALL Java_test_JNIUtility_testJavaShort
  (JNIEnv *env, jclass clazz, jshort java_val) {
	short native_short = ((short) java_val -1);
	std::cout << "Java [" << java_val << "] - 1 = native {" << native_short  << "}" << std::endl;

	return (jshort) native_short;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaInteger
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_test_JNIUtility_testJavaInteger
  (JNIEnv *env, jclass clazz, jint java_val) {
	int native_val = ((int) java_val -1);
	std::cout << "Java [" << java_val << "] - 1 = native {" << native_val  << "}" << std::endl;

	return (jint) native_val;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaLong
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_test_JNIUtility_testJavaLong
  (JNIEnv *env, jclass clazz, jlong java_val) {
	long native_val = ((long) java_val -1);
	std::cout << "Java [" << java_val << "] - 1 = native {" << native_val  << "}" << std::endl;

	return (jlong) native_val;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaFloat
 * Signature: (F)F
 */
JNIEXPORT jfloat JNICALL Java_test_JNIUtility_testJavaFloat
  (JNIEnv *env, jclass clazz, jfloat java_val) {
	float native_val = ((float) java_val -1);
	std::cout << "Java [" << java_val << "] - 1 = native {" << native_val  << "}" << std::endl;

	return (jfloat) native_val;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaDouble
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_test_JNIUtility_testJavaDouble
  (JNIEnv *env, jclass clazz, jdouble java_val) {
	double native_val = ((double) java_val -1);
	std::cout << "Java [" << java_val << "] - 1 = native {" << native_val  << "}" << std::endl;

	return (jdouble	) native_val;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaString
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_test_JNIUtility_testJavaString
  (JNIEnv *env, jclass clazz, jstring java_str, jstring java_encoding_str) {	
	string native_encoding_str = jni::DataTypeUtils::getNativeString(env, java_encoding_str);
	string native_str = jni::DataTypeUtils::getNativeString(env, java_str);
	
	string original_native_str = string(native_str);
	reverse_string(native_str);

	jstring result_java_str = jni::DataTypeUtils::getJavaStringWithEncoding(env, native_str.c_str(), native_encoding_str.c_str());
	string result_native_str = jni::DataTypeUtils::getNativeStringWithEncoding(env, result_java_str, native_encoding_str.c_str());
	
	std::cout << "Java [" << original_native_str << "] -> reverse = native {" << result_native_str << "}" << std::endl;

	return result_java_str;
}

void reverse_string(string &str)
{       // Reverse the string contained in str

        char temp;
        
        for (unsigned int i = 0; i < str.length() / 2; i++)
        {
        	temp = str[i];
        	str[i] = str[str.length()-i-1];
        	str[str.length()-i-1] = temp;
        }
} 

/*
 * Class:     test_JNIUtility
 * Method:    testJavaObject
 * Signature: (Ltest/Point;)Ltest/Point;
 */
JNIEXPORT jobject JNICALL Java_test_JNIUtility_testJavaObject
  (JNIEnv *env, jclass clazz, jobject j_obj) {

	//Get Class Object
	jclass j_obj_clazz = env->GetObjectClass(j_obj);
	if(j_obj_clazz == NULL) {
		return NULL;
	}

	//Get Field, X and Y with given object class
	jfieldID j_obj_field_x = env->GetFieldID(j_obj_clazz, "_x", "I");
	if(j_obj_field_x == NULL) {
		return NULL;
	}

	jfieldID j_obj_field_y = env->GetFieldID(j_obj_clazz, "_y", "I");
	if(j_obj_field_y == NULL) {
		return NULL;
	}

	//Get Field values of X and Y with **jobject** instead of jclass
	jint jint_x = env->GetIntField(j_obj, j_obj_field_x);
	jint jint_y = env->GetIntField(j_obj, j_obj_field_y);
	
	jint_x = jint_x - 1;
	jint_y = jint_y - 1;

	/* 
	 * Creating new Object Point
	 */

	//Retrieve Method "Point.Point(II)"
	jmethodID ctorID = env->GetMethodID(j_obj_clazz, "<init>",
			"(II)V");

	if(ctorID == NULL) {
		return NULL;
	}

	//Create a Java Point Object
	jobject j_result = env->NewObject(j_obj_clazz, ctorID, jint_x, jint_y);

	//delete local reference to Point class
	env->DeleteLocalRef(j_obj_clazz);

	return j_result;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaByteArray
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_test_JNIUtility_testJavaByteArray
  (JNIEnv *env, jclass clazz, jbyteArray j_array) {
	vector<char> cChars;

	std::cout << "Convert to native array.." << std::endl;
	jni::DataTypeUtils::getNativeByteArray(env, j_array, cChars);

	/*
	for(unsigned int i = 0; i < cChars.size(); i++) {
		std::cout << (int) cChars[i] << " ";
	}
	std::cout << std::endl;
	*/

	std::cout << "Convert to Java array given native data.." << std::endl;
	jbyteArray j_result = jni::DataTypeUtils::getJavaByteArray(env, cChars);

	return j_result;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaShortArray
 * Signature: ([S)[S
 */
JNIEXPORT jshortArray JNICALL Java_test_JNIUtility_testJavaShortArray
(JNIEnv *env, jclass clazz, jshortArray j_array) {
	vector<short> cShorts;

	std::cout << "Convert to native array.." << std::endl;
	jni::DataTypeUtils::getNativeShortArray(env, j_array, cShorts);

	/*
	for(unsigned int i = 0; i < cShorts.size(); i++) {
		std::cout << cShorts[i] << " ";
	}
	std::cout << std::endl;
	*/

	std::cout << "Convert to Java array given native data.." << std::endl;
	jshortArray j_result = jni::DataTypeUtils::getJavaShortArray(env, cShorts);

	return j_result;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaIntegerArray
 * Signature: ([I)[I
 */
JNIEXPORT jintArray JNICALL Java_test_JNIUtility_testJavaIntegerArray
(JNIEnv *env, jclass clazz, jintArray j_array) {
	vector<int> cInts;

	std::cout << "Convert to native array.." << std::endl;
	jni::DataTypeUtils::getNativeIntegerArray(env, j_array, cInts);

	/*
	for(unsigned int i = 0; i < cInts.size(); i++) {
		std::cout << cInts[i] << " ";
	}
	std::cout << std::endl;
	*/

	std::cout << "Convert to Java array given native data.." << std::endl;
	jintArray j_result = jni::DataTypeUtils::getJavaIntegerArray(env, cInts);

	return j_result;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaLongArray
 * Signature: ([J)[J
 */
JNIEXPORT jlongArray JNICALL Java_test_JNIUtility_testJavaLongArray
(JNIEnv *env, jclass clazz, jlongArray j_array) {
	vector<long> cLongs;

	std::cout << "Convert to native array.." << std::endl;
	jni::DataTypeUtils::getNativeLongArray(env, j_array, cLongs);

	/*
	for(unsigned int i = 0; i < cInts.size(); i++) {
		std::cout << cInts[i] << " ";
	}
	std::cout << std::endl;
	*/

	std::cout << "Convert to Java array given native data.." << std::endl;
	jlongArray j_result = jni::DataTypeUtils::getJavaLongArray(env, cLongs);

	return j_result;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaFloatArray
 * Signature: ([F)[F
 */
JNIEXPORT jfloatArray JNICALL Java_test_JNIUtility_testJavaFloatArray
(JNIEnv *env, jclass clazz, jfloatArray j_array) {
	vector<float> cFloats;

	std::cout << "Convert to native array.." << std::endl;
	jni::DataTypeUtils::getNativeFloatArray(env, j_array, cFloats);
	
	/*
	for(unsigned int i = 0; i < cFloats.size(); i++) {
		std::cout << cFloats[i] << " ";
	}
	std::cout << std::endl;
	*/

	std::cout << "Convert to Java array given native data.." << std::endl;
	jfloatArray j_result = jni::DataTypeUtils::getJavaFloatArray(env, cFloats);

	return j_result;
}


/*
 * Class:     test_JNIUtility
 * Method:    testJavaDoubleArray
 * Signature: ([D)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_test_JNIUtility_testJavaDoubleArray
(JNIEnv *env, jclass clazz, jdoubleArray j_array) {
	vector<double> cDoubles;

	std::cout << "Convert to native array.." << std::endl;
	jni::DataTypeUtils::getNativeDoubleArray(env, j_array, cDoubles);

	/*
	for(unsigned int i = 0; i < cDoubles.size(); i++) {
		std::cout << cDoubles[i] << " ";
	}
	std::cout << std::endl;
	*/

	std::cout << "Convert to Java array given native data.." << std::endl;
	jdoubleArray j_result = jni::DataTypeUtils::getJavaDoubleArray(env, cDoubles);

	return j_result;
}

/*
 * Class:     test_JNIUtility
 * Method:    testJavaStringArray
 * Signature: ([Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_test_JNIUtility_testJavaStringArray
(JNIEnv *env, jclass clazz, jobjectArray j_array, jstring j_encoding_str) {
  vector<string> cStrings;

  std::string native_encoding = jni::DataTypeUtils::getNativeString(env, j_encoding_str);

  std::cout << "Convert to native array.." << std::endl;
  jni::DataTypeUtils::getNativeStringArrayWithEncoding(env, j_array, cStrings, native_encoding.c_str());
  
  for(unsigned i = 0; i < cStrings.size(); i++) {
    reverse_string(cStrings[i]);
  }
  
  std::cout << "Convert to Java array given native data.." << std::endl;
  jobjectArray j_result = jni::DataTypeUtils::getJavaStringArrayWithEncoding(env, cStrings, native_encoding.c_str());

  return j_result;
}


/*
 * Class:     test_JNIUtility
 * Method:    test2DJavaByteArray
 * Signature: ([[B)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_test_JNIUtility_test2DJavaByteArray
(JNIEnv *env, jclass clazz, jobjectArray j_array) {
  vector< vector<char> > cChars;

  std::cout << "Convert to native array.." << std::endl;
  jni::DataTypeUtils::getNative2DByteArray(env, j_array, cChars);
  
  /*
  for(unsigned int i = 0; i < cChars.size(); i++) {
    std::cout << (int) cChars[i] << " ";
  }
  std::cout << std::endl;
  */

  jobjectArray j_result = jni::DataTypeUtils::getJava2DByteArray(env, cChars);
  
  return j_result;
}

/*}
 * Class:     test_JNIUtility
 * Method:    testThrowOutOfMemoryError
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_test_JNIUtility_testThrowOutOfMemoryError
  (JNIEnv *env, jclass clazz) {
	jni::ExceptionUtils::throwOutOfMemoryError(env, "Caught in Native testThrowOutOfMemoryError()");
}

/*
 * Class:     test_JNIUtility
 * Method:    testThrowInternalError
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_test_JNIUtility_testThrowInternalError
  (JNIEnv *env, jclass clazz) {
	jni::ExceptionUtils::throwInternalError(env, "Caught in Native throwInternalError()");
}

/*
 * Class:     test_JNIUtility
 * Method:    testThrowNullPointerException
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_test_JNIUtility_testThrowNullPointerException
  (JNIEnv *env, jclass clazz) {
	jni::ExceptionUtils::throwNullPointerException(env, "Caught in Native testThrowNullPointerException()");
}

/*
 * Class:     test_JNIUtility
 * Method:    testThrowIllegalStateException
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_test_JNIUtility_testThrowIllegalStateException
  (JNIEnv *env, jclass clazz) {
	jni::ExceptionUtils::throwIllegalStateException(env, "Caught in Native testThrowIllegalStateException()");
}

/*
 * Class:     test_JNIUtility
 * Method:    testThrowIllegalArgumentException
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_test_JNIUtility_testThrowIllegalArgumentException
  (JNIEnv *env, jclass clazz) {
	jni::ExceptionUtils::throwIllegalArgumentException(env, "Caught in Native testThrowIllegalArgumentException()");
}

/*
 * Class:     test_JNIUtility
 * Method:    testThrowUnsupportedOperationException
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_test_JNIUtility_testThrowUnsupportedOperationException
  (JNIEnv *env, jclass clazz) {
	jni::ExceptionUtils::throwUnsupportedOperationException(env, "Caught in Native testThrowUnsupportedOperationException()");
}
