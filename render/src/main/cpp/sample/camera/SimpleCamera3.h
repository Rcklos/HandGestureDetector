//
// Created by 20152 on 2023/1/9.
//
#pragma once
#include "GLBaseSample.h"

class SimpleCamera3: public GLBaseSample {
public:

    void Create() override;
    void Draw() override;
    void Shutdown() override;
    void SetDirection(int dir) override;

private:
    glm::mat4 m_MVPMatrix;
    glm::mat4 m_ModelMatrix;

    Shader *mShader;
    Model *mModel;

    glm::mat4 mvp;

    float computeDeltaTime();
    void update(float deltaTime);
    long mLastTime = 0;
    float degree = 0;

    int mDir = GL_DIRECTION_NONE;
};
