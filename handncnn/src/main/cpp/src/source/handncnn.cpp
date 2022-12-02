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


static Hand* g_hand = 0;
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

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_mediapipe_ncnn_NCCNHandDetector_detect(JNIEnv *env, jobject thiz, jobject bitmap,
                                                      jobject result) {
    cv::Mat rgb;
    bitmapToMat(env, bitmap, rgb);
    cv::cvtColor(rgb, rgb, cv::COLOR_RGBA2RGB);
    std::vector<PalmObject> objects;
    if(g_hand) {
        std::vector<PalmObject> objects;
        g_hand->detect(rgb, objects);
        g_hand->draw(rgb, objects);
        draw_fps(rgb);
        matToBitmap(env, rgb, bitmap);
    } else {
        LOGI("g_hand ====== null");
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_cn_lentme_mediapipe_ncnn_NCCNHandDetector_load(JNIEnv *env, jobject thiz,
                                                    jobject asset_manager) {
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