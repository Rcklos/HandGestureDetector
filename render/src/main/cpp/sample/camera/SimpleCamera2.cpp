//
// Created by 20152 on 2023/1/9.
//

#include "SimpleCamera2.h"
#include "GLUtils.h"
#include <string>

void SimpleCamera2::Create() {
    // 加载模型
    mModel = new Model(std::string(cacheDir) + "/object/town/old_town_block.obj");

    // 加载渲染器
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/simple_load_model.glsl");
    if(mModel->ContainsTextures())
        FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/simple_load_model2.glsl");
    else
        FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/simple_load_model.glsl");
    mShader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);

    // 清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
    glEnable(GL_DEPTH_TEST);
}

void SimpleCamera2::Draw() {
    if(mShader == nullptr || mModel == nullptr) return;
//    LOGD("SimpleCamera2::Draw()");

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // 使用渲染器
    mShader->use();

    // 设置mvp矩阵
    update(computeDeltaTime());
    mShader->setMat4("uMVPMatrix", mvp);

    // 绘制
    mModel->Draw(*mShader);
}

void SimpleCamera2::Shutdown() {
    GLBaseSample::Shutdown();
    // 销毁模型
    if(mModel != nullptr) {
        mModel->Destroy();
        delete mModel;
        mModel = nullptr;
    }

    // 销毁渲染器
    if(mShader != nullptr) {
        mShader->Destroy();
        delete mShader;
        mShader = nullptr;
    }
}

float SimpleCamera2::computeDeltaTime() {
    if (mLastTime == 0) {
        mLastTime = TimeUtils::currentTimeMillis();
    }
    long currentTime = TimeUtils::currentTimeMillis();
    long elapsedTime = currentTime - mLastTime;
    float deltaTime = (float) elapsedTime / 1000.0f;
    mLastTime = currentTime;
    return deltaTime;
}

void SimpleCamera2::update(float deltaTime) {
    if(degree < 360.f) degree += 15.f * deltaTime;
    else degree = 0;

    auto aspect = (GLfloat) m_Width / (GLfloat) m_Height;
    auto scale = 1.0f;

    glm::mat4 proj = glm::perspective(glm::radians(45.f), aspect, .1f, 500.f);

    // 摄像机
    auto angle = degree / 360.f * 2 * MATH_PI;
    float camX = sin(angle);
    float camZ = cos(angle);
//    LOGD("camera: camX=%.2lf, camZ=%.2lf", camX, camZ)
    auto view = glm::lookAt(
            // location
            glm::vec3(0, 0.0f, 0),
            // direction
            glm::normalize(glm::vec3(camX, 0.0f, camZ)),
            // up
            glm::vec3(0.0f, 1.0f, 0.0f)
            );

    auto model = glm::mat4(1.f);
    // 位移到视野看得到的地方
    model = glm::translate(model, glm::vec3(0.f, -(scale * 80), 0.f));
    // 缩放
    model = glm::scale(model, glm::vec3(scale, scale, scale));
    mvp = proj * view * model;
}
