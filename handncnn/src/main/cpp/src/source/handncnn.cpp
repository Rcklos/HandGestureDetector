#include <jni.h>
#include <string>
#include <ctime>
#include <opencv2/core.hpp>

extern "C" JNIEXPORT jstring JNICALL
Java_cn_lentme_mediapipe_ncnn_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}