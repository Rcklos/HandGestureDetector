//
// Created by 20152 on 2023/1/9.
//
#pragma once
#include "GLBaseSample.h"

class SimpleCamera4: public GLBaseSample {
public:

    void Create() override;
    void Draw() override;
    void Shutdown() override;
    void SetDelta(float x, float y) override;
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
    float degree_x = 0;
    glm::vec3 eye = glm::vec3(0, 0, 0);

    delta_t mDelta = { -1.0f, -1.0f };
    delta_t mLast = { -1.0f, -1.0f };
    int mDir = GL_DIRECTION_NONE;
};
