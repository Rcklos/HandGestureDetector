//
// Created by 20152 on 2023/1/7.
//

#pragma once
#include "GLBaseSample.h"
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>

class SimpleRotate: public GLBaseSample {
public:
    const unsigned int VERTEX_POS_INDX = 0;
public:
    SimpleRotate() = default;
    virtual ~SimpleRotate() = default;
    void Create() override;
    void Draw() override;

private:
    float computeDeltaTime();
    void update(float deltaTime);
    long mLastTime = 0;

    float degree = 0.0f;
    glm::mat4 trans;
    GLuint transformLoc;
};
