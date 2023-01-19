//
// Created by 20152 on 2023/1/7.
//
#pragma once
#include "GLBaseSample.h"

#define TEX_R           255, 0, 0
#define TEX_G           0, 255, 0
#define TEX_B           0, 0, 255

// 第二个例子，颜色由用户控制，不再写死到shader
class HelloRectangle3: public GLBaseSample {
public:
    static constexpr const unsigned int VERTEX_POS_INDX = 0;
    static constexpr const unsigned int VERTEX_POS_SIZE = 3;

    static constexpr const unsigned int VERTEX_TEX_INDX = 1;
    static constexpr const unsigned int VERTEX_TEX_SIZE = 2;

    static constexpr const unsigned int INDICES_SIZE = 6;
public:
    HelloRectangle3() = default;
    virtual ~HelloRectangle3() = default;
    void Create() override;
    void Draw() override;

private:
    GLuint vboIds[3];

    GLuint samplerLoc;
    GLuint textureId;
    static GLuint CreateRectangleTexture2D();
};
