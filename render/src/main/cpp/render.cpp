#include <jni.h>
#include <string>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <GLUtils.h>
#include <shader.h>
#include <GLBaseSample.h>
#include <GLBaseSampleInc.h>


GLBaseSample *sample = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_gles_render_MyNativeRender_glesCreate(JNIEnv *env, jobject thiz, jobject asset_mgr) {
    GLUtils::setEnvAndAssetManager(env, asset_mgr);
    if(sample != nullptr) {
        sample->Create();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_gles_render_MyNativeRender_glesResize(JNIEnv *env, jobject thiz, jint width,
                                                     jint height) {
    if(sample != nullptr) {
        sample->Change(width, height);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_gles_render_MyNativeRender_glesDraw(JNIEnv *env, jobject thiz) {
    if(sample != nullptr) {
        sample->Draw();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_gles_render_MyNativeRender_setDelta(JNIEnv *env, jobject thiz, jfloat x, jfloat y) {
    if(sample != nullptr)
        sample->SetDelta(x, y);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_gles_render_MyNativeRender_setDirection(JNIEnv *env, jobject thiz, jint dir) {
    sample->SetDirection(dir);
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_lentme_gles_render_MyNativeRender_glesSetType(JNIEnv *env, jobject thiz, jint type) {
    switch(type) {
        case GL_HELLO_TRIANGLE:
            sample = new HelloTriangle();
            break;
        case GL_HELLO_TRIANGLE2:
            sample = new HelloTriangle2();
            break;
        case GL_HELLO_TRIANGLE3:
            sample = new HelloTriangle3();
            break;
        case GL_HELLO_RECTANGLE:
            sample = new HelloRectangle();
            break;
        case GL_HELLO_RECTANGLE2:
            sample = new HelloRectangle2();
            break;
        case GL_HELLO_RECTANGLE3:
            sample = new HelloRectangle3();
            break;
        case GL_SIMPLE_SCALE:
            sample = new SimpleScale();
            break;
        case GL_SIMPLE_TRANSLATE:
            sample = new SimpleTranslate();
            break;
        case GL_SIMPLE_ROTATE:
            sample = new SimpleRotate();
            break;
        case GL_HELLO_CUBE:
            sample = new HelloCube();
            break;
        case GL_HELLO_CUBE2:
            sample = new HelloCube2();
            break;
        case GL_HELLO_CUBE3:
            sample = new HelloCube3();
            break;
        case GL_SIMPLE_LOAD_MODEL:
            sample = new SimpleLoadModel();
            break;
        case GL_SIMPLE_LOAD_MODEL2:
            sample = new SimpleLoadModel2();
            break;
        case GL_SIMPLE_CAMERA:
            sample = new SimpleCamera();
            break;
        case GL_SIMPLE_CAMERA2:
            sample = new SimpleCamera2();
            break;
        case GL_SIMPLE_CAMERA3:
            sample = new SimpleCamera3();
            break;
        case GL_SIMPLE_CAMERA4:
            sample = new SimpleCamera4();
            break;
        default:
            LOGE("sample type not found!!!!");
            break;
    }
}