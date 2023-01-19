//
// Created by 20152 on 2023/1/9.
//

#include "SimpleLoadModel.h"
#include <string>

void SimpleLoadModel::Create() {
    // 加载模型
    mModel = new Model(std::string(cacheDir) + "/object/boxes/box.obj");

    // 加载渲染器
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/simple_load_model.glsl");
    FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/simple_load_model.glsl");
    mShader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);

    // 清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
    glEnable(GL_DEPTH_TEST);
}

void SimpleLoadModel::Draw() {
    if(mShader == nullptr || mModel == nullptr) return;
//    LOGD("SimpleLoadModel::Draw()");

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // 使用渲染器
    mShader->use();

    // 设置mvp矩阵
    update(computeDeltaTime());
    mShader->setMat4("uMVPMatrix", mvp);

    // 绘制
    mModel->Draw(*mShader);
}

void SimpleLoadModel::Shutdown() {
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

float SimpleLoadModel::computeDeltaTime() {
    if (mLastTime == 0) {
        mLastTime = TimeUtils::currentTimeMillis();
    }
    long currentTime = TimeUtils::currentTimeMillis();
    long elapsedTime = currentTime - mLastTime;
    float deltaTime = (float) elapsedTime / 1000.0f;
    mLastTime = currentTime;
    return deltaTime;
}

void SimpleLoadModel::update(float deltaTime) {
    if(degree < 360.f) degree += 15.f * deltaTime;
    else degree = 0;

    auto aspect = (GLfloat) m_Width / (GLfloat) m_Height;
    auto scale = .7f;

    /**
     * mvp变换顺序:
     * 1. 先对模型(model)进行变换，主要有缩放、平移、旋转
     * 2. 摆好摄像机的位置
     * 3. 投影成像
     * mvp的变换顺序实际上对应着三个坐标系，即三种空间：
     * 0. 局部空间(顶点位置定义，不在变换空间讨论范围内)
     * 1. 世界空间
     * 2. 观察空间
     * 3. 裁剪空间
     * 进行矩阵变换时，需要考虑到顺序: 先平移，在旋转，最后缩放
     * 也就是和所谓的[先缩放再旋转再平移]的操作是反过来的
     * 原因是书写时矩阵是矩阵行主序的，而OpenGL ES中又是列主序
     * 不管行列主序，在数学意义上操作是全等的
     * 只是实际操作起来的时候，需要倒过来，例如mvp变换就变成了[pvm]
     */
    glm::mat4 proj = glm::perspective(glm::radians(45.f), aspect, .1f, 20.f);
    auto model = glm::mat4(1.f);
    // 位移到视野看得到的地方
    model = glm::translate(model, glm::vec3(0.f, 0.f, -10.f));
    // 旋转立方体
    model = glm::rotate(model, glm::radians(degree), glm::vec3(-1.f, -1.f, 0.f));
    // 缩放
    model = glm::scale(model, glm::vec3(scale, scale, scale));
    mvp = proj * model;
}
