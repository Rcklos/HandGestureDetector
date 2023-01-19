//
// Created by 20152 on 2023/1/7.
//

#ifndef NDK_OPENGLES_LEARN_HELLOTRIANGLE_H
#define NDK_OPENGLES_LEARN_HELLOTRIANGLE_H
#include "GLBaseSample.h"

class HelloTriangle: public GLBaseSample {
public:
    const unsigned int VERTEX_POS_INDX = 0;
public:
    HelloTriangle() = default;
    virtual ~HelloTriangle() = default;
    void Create() override;
    void Draw() override;
};


#endif //NDK_OPENGLES_LEARN_HELLOTRIANGLE_H
