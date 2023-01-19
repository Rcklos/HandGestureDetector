//
// Created by 20152 on 2023/1/9.
//

#include "SimpleCamera4.h"
#include "GLUtils.h"
#include <string>

void SimpleCamera4::Create() {
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

void SimpleCamera4::Draw() {
    if(mShader == nullptr || mModel == nullptr) return;
//    LOGD("SimpleCamera4::Draw()");

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // 使用渲染器
    mShader->use();

    // 设置mvp矩阵
    update(computeDeltaTime());
    mShader->setMat4("uMVPMatrix", mvp);

    // 绘制
    mModel->Draw(*mShader);
}

void SimpleCamera4::Shutdown() {
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

float SimpleCamera4::computeDeltaTime() {
    if (mLastTime == 0) {
        mLastTime = TimeUtils::currentTimeMillis();
    }
    long currentTime = TimeUtils::currentTimeMillis();
    long elapsedTime = currentTime - mLastTime;
    float deltaTime = (float) elapsedTime / 1000.0f;
    mLastTime = currentTime;
    return deltaTime;
}

void SimpleCamera4::update(float deltaTime) {
    auto aspect = (GLfloat) m_Width / (GLfloat) m_Height;
    auto scale = 1.0f;

    glm::mat4 proj = glm::perspective(glm::radians(45.f), aspect, .1f, 500.f);

    // 摄像机
    auto unit = 15.f * deltaTime;
    // 控制摄像机旋转
    auto delta = mDelta;
    auto dx = delta.x - mLast.x;
    if(delta.x != -1.0f && dx != 0) {
        if(mLast.x != -1.0f) {
            degree_x += -dx * 45.0f;
        }
    }
    mLast = delta;
//    if(mDir == GL_DIRECTION_LEFT) degree_x += unit;
//    if(mDir == GL_DIRECTION_RIGHT) degree_x -= unit;
    auto angle = degree_x / 360.f * 2 * MATH_PI;
    float camX = sin(angle);
    float camZ = cos(angle);
    glm::vec3 direction = glm::normalize(glm::vec3(camX, 0.0f, camZ));
    glm::vec3 up = glm::vec3(0, 1.0f, 0);

    // 移动摄像机
    auto moveUnit = 35 * deltaTime;
    if(mDir == GL_DIRECTION_UP) eye += moveUnit * direction;
    if(mDir == GL_DIRECTION_DOWN) eye -= moveUnit * direction;
    if(mDir == GL_DIRECTION_LEFT) eye -= glm::cross(direction, up) * moveUnit;
    if(mDir == GL_DIRECTION_RIGHT) eye += glm::cross(direction, up) * moveUnit;

    auto center = eye + direction;
//    LOGD("camera: camX=%.2lf, camZ=%.2lf", camX, camZ)
    auto view = glm::lookAt(eye, center, up);

    auto model = glm::mat4(1.f);
    // 位移到视野看得到的地方
    model = glm::translate(model, glm::vec3(0.f, -(scale * 80), 0.f));
    // 缩放
    model = glm::scale(model, glm::vec3(scale, scale, scale));
    mvp = proj * view * model;
}

void SimpleCamera4::SetDirection(int dir) {
    GLBaseSample::SetDirection(dir);
    mDir = dir;
}

void SimpleCamera4::SetDelta(float x, float y) {
    GLBaseSample::SetDelta(x, y);
    mDelta = { x, y };
    LOGD("delta: %.2f, %.2f", x, y);
}

