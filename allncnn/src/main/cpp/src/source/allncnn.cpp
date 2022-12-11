#include <android/asset_manager_jni.h>
#include <jni.h>
#include <string>
#include <ctime>
#include <opencv2/core.hpp>
#include <BitmapUtils.h>
#include <cpu.h>
#include <mat.h>
#include <hand.h>
#include <benchmark.h>
#include <OcrUtils.h>
#include <yolo.h>
#include <jni.h>

#define CLASS_POINT2F "cn/lentme/allncnn/Point2f"
#define CLASS_DETECTRESULT "cn/lentme/allncnn/DetectResult"
#define CLASS_YOLO_OBJECT   "cn/lentme/allncnn/YoloObject"


static Hand* g_hand = 0;
static Yolo* g_yolo = 0;
static ncnn::Mutex lock;

extern "C" JNIEXPORT jstring JNICALL
Java_cn_lentme_mediapipe_ncnn_NCCNHandDetector_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

static int draw_fps(cv::Mat& rgb)
{
    // resolve moving average
    float avg_fps = 0.f;
    {
        static double t0 = 0.f;
        static float fps_history[10] = {0.f};

        double t1 = ncnn::get_current_time();
        if (t0 == 0.f)
        {
            t0 = t1;
            return 0;
        }

        float fps = 1000.f / (t1 - t0);
        t0 = t1;

        for (int i = 9; i >= 1; i--)
        {
            fps_history[i] = fps_history[i - 1];
        }
        fps_history[0] = fps;

        if (fps_history[9] == 0.f)
        {
            return 0;
        }

        for (int i = 0; i < 10; i++)
        {
            avg_fps += fps_history[i];
        }
        avg_fps /= 10.f;
    }

    char text[32];
    sprintf(text, "FPS=%.2f", avg_fps);

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

    int y = 0;
    int x = rgb.cols - label_size.width;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                  cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));

    return 0;
}

jclass newJListClass(JNIEnv *jniEnv) {
    jclass clazz = jniEnv->FindClass("java/util/ArrayList");
    if (clazz == NULL) {
        LOGE("ArrayList class is null");
        return NULL;
    }
    return clazz;
}

void buildArrayPoint2F(JNIEnv *env, PalmObject &object, jobject &list) {
    jclass jListClass = newJListClass(env);
    jmethodID jAddMethod = env->
            GetMethodID(jListClass, "add", "(Ljava/lang/Object;)Z");
    jclass jPoint2FClass = env->FindClass(CLASS_POINT2F);
    jmethodID jPoint2FConstructor = env->
            GetMethodID(jPoint2FClass, "<init>", "(FF)V");
    for(int i = 0; i < object.skeleton.size(); i++) {
        jobject jPoint2F = env->NewObject(jPoint2FClass, jPoint2FConstructor,
                                          object.skeleton[i].x, object.skeleton[i].y);
        env->CallBooleanMethod(list, jAddMethod, jPoint2F);
    }
}

void buildArrayObject(JNIEnv *env, std::vector<PalmObject> &objects, jobject &list) {
    jclass jListClass = newJListClass(env);
    jmethodID jListConstructor = env->GetMethodID(jListClass, "<init>", "()V");
    jmethodID jAddMethod = env->
            GetMethodID(jListClass, "add", "(Ljava/lang/Object;)Z");
    for(auto object: objects) {
        jobject newList = env->NewObject(jListClass, jListConstructor);
        buildArrayPoint2F(env, object, newList);
        env->CallBooleanMethod(list, jAddMethod, newList);
    }
}

jboolean load_hand_detector(JNIEnv *env, jobject thiz, jobject asset_manager) {
    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
    const char* palm_type = "palm-full";
    const int target_size_ = 192;

    const float mean_val[3] = {0.f,0.f,0.f};
    const float norm_val[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};

    {
        ncnn::MutexLockGuard g(lock);
        if(ncnn::get_gpu_count() == 0) {
            delete g_hand;
            g_hand = nullptr;
        }
        else {
            if(!g_hand) g_hand = new Hand;
            // 暂时写死gpu
            if(g_hand->load(mgr, palm_type, target_size_,
                             mean_val, norm_val, 1) == 0)
                LOGI("g_hand loaded!!!");
            else LOGI("g_hand load failed!!!");
        }
    }

    return JNI_TRUE;
}

jboolean load_yolo_detector(JNIEnv *env, jobject thiz, jobject asset_manager) {
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

jobject detect_hand(JNIEnv *env, jobject thiz, jobject bitmap) {
    cv::Mat rgb;
    bitmapToMat(env, bitmap, rgb);
    cv::cvtColor(rgb, rgb, cv::COLOR_RGBA2RGB);
    std::vector<PalmObject> objects;

    jclass jListClass = newJListClass(env);
    jmethodID jListConstructor = env->GetMethodID(jListClass,
                                                  "<init>", ("()V"));
    jclass jResultClass = env->FindClass(CLASS_DETECTRESULT);
    jmethodID jResultConstructor = env->
            GetMethodID(jResultClass, "<init>", "(Ljava/util/List;)V");
    jobject jList = env->NewObject(jListClass, jListConstructor);

    if(g_hand) {
        std::vector<PalmObject> objects;
        g_hand->detect(rgb, objects);
        g_hand->draw(rgb, objects);
        draw_fps(rgb);
        matToBitmap(env, rgb, bitmap);

        buildArrayObject(env, objects, jList);
    } else {
        LOGI("g_hand ====== null");
    }
    jobject jResult = env->NewObject(jResultClass, jResultConstructor, jList);
    return jResult;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_lentme_allncnn_NCNNService_loadHandDetector(JNIEnv *env, jobject thiz,
                                                    jobject asset_manager) {
    return load_hand_detector(env, thiz, asset_manager);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_cn_lentme_allncnn_NCNNService_detectHand(JNIEnv *env, jobject thiz, jobject bitmap) {
    return detect_hand(env, thiz, bitmap);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_lentme_allncnn_NCNNService_loadYoloDetector(JNIEnv *env, jobject thiz,
                                                    jobject asset_manager) {
    return load_yolo_detector(env, thiz, asset_manager);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_cn_lentme_allncnn_NCNNService_detectYolo(JNIEnv *env, jobject thiz, jobject bitmap) {
    cv::Mat rgb;
    bitmapToMat(env, bitmap, rgb);
    cv::cvtColor(rgb, rgb, cv::COLOR_RGBA2RGB);
    std::vector<Object> objects;

    jclass jListClass = newJListClass(env);
    jmethodID jListConstructor = env->GetMethodID(jListClass,
                                                  "<init>", ("()V"));
    jmethodID jAdd = env->GetMethodID(jListClass, "add", "(Ljava/lang/Object;)Z");
    jclass jYoloObject = env->FindClass(CLASS_YOLO_OBJECT);
    jmethodID jYoloObjectConstructor = env->
            GetMethodID(jYoloObject, "<init>", "(Landroid/graphics/RectF;IF)V");
    jclass jRectF = env->FindClass("android/graphics/RectF");
    jmethodID jRectFConstructor = env->GetMethodID(jRectF, "<init>", "(FFFF)V");
    g_yolo->detect(rgb, objects);
    g_yolo->draw(rgb, objects);

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