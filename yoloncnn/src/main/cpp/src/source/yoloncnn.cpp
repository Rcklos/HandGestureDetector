#include <jni.h>
#include <android/asset_manager_jni.h>
#include <string>
#include <platform.h>
#include <gpu.h>
#include <yolo.h>
#include <opencv2/imgproc.hpp>
#include <BitmapUtils.h>

static ncnn::Mutex lock;
static Yolo* g_yolo = 0;

jclass newJListClass(JNIEnv *jniEnv) {
    jclass clazz = jniEnv->FindClass("java/util/ArrayList");
    if (clazz == NULL) {
        return NULL;
    }
    return clazz;
}

extern "C" JNIEXPORT jstring JNICALL
Java_cn_lentme_yolo_ncnn_YoloNCNN_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jobject JNICALL
Java_cn_lentme_yolo_ncnn_YoloNCNN_detect(JNIEnv *env, jobject thiz, jobject bitmap) {
    cv::Mat rgb;
    bitmapToMat(env, bitmap, rgb);
    cv::cvtColor(rgb, rgb, cv::COLOR_RGBA2RGB);
    std::vector<Object> objects;

    jclass jListClass = newJListClass(env);
    jmethodID jListConstructor = env->GetMethodID(jListClass,
                                                  "<init>", ("()V"));
    jmethodID jAdd = env->GetMethodID(jListClass, "add", "(Ljava/lang/Object;)V");
    jclass jYoloObject = env->FindClass("cn/lentme/yolo/ncnn/YoloObject");
    jmethodID jYoloObjectConstructor = env->
            GetMethodID(jYoloObject, "<init>", "(Landroid/graphics/RectF;IF)V");
    jclass jRectF = env->FindClass("android/graphics/RectF");
    jmethodID jRectFConstructor = env->GetMethodID(jRectF, "<init>", "(FFFF)V");
    g_yolo->detect(rgb, objects);
//    g_yolo->draw(rgb, objects);

    jobject jList = env->NewObject(jListClass, jListConstructor);
    for(auto obj: objects) {
        jobject rectF = env->NewObject(jRectF, jRectFConstructor,
                                       obj.rect.x, obj.rect.y,
                                       obj.rect.x + obj.rect.width, obj.rect.y + obj.rect.height);
        jobject jobj = env->NewObject(jYoloObject, jYoloObjectConstructor,
                                      rectF, obj.label, obj.prob);
        env->CallBooleanMethod(jList, jAdd, jobj);
    }
    return jList;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_lentme_yolo_ncnn_YoloNCNN_load(JNIEnv *env, jobject thiz, jobject asset_manager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, asset_manager);
    const char *model_type = "yolov7-tiny";
    const int target_size_ = 640;
    const float normal_val[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};

    {
        ncnn::MutexLockGuard g(lock);
        if(ncnn::get_gpu_count() == 0) {
            delete g_yolo;
            g_yolo = nullptr;
        }
        else {
            if(!g_yolo)
                g_yolo = new Yolo;
            g_yolo->load(mgr, model_type,
                         target_size_, normal_val, 1);
        }
    }
    return JNI_TRUE;
}