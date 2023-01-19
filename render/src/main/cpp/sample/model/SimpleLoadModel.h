//
// Created by 20152 on 2023/1/9.
//

#ifndef NDK_OPENGLES_LEARN_SIMPLELOADMODEL_H
#define NDK_OPENGLES_LEARN_SIMPLELOADMODEL_H

#include "GLBaseSample.h"

class SimpleLoadModel: public GLBaseSample {
public:
    void Create() override;
    void Draw() override;
    void Shutdown() override;

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
};


#endif //NDK_OPENGLES_LEARN_SIMPLELOADMODEL_H
