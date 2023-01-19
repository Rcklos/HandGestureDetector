//
// Created by 20152 on 2023/1/7.
//

#ifndef NDK_OPENGLES_LEARN_SIMPLESCALE_H
#define NDK_OPENGLES_LEARN_SIMPLESCALE_H
#include "GLBaseSample.h"
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>

class SimpleScale: public GLBaseSample {
public:
    const unsigned int VERTEX_POS_INDX = 0;
public:
    SimpleScale() = default;
    virtual ~SimpleScale() = default;
    void Create() override;
    void Draw() override;

private:
    float computeDeltaTime();
    void update(float deltaTime);
    long mLastTime = 0;

    float scale = 1.0f;
    glm::mat4 trans;
    GLuint transformLoc;
};


#endif //NDK_OPENGLES_LEARN_SIMPLESCALE_H
