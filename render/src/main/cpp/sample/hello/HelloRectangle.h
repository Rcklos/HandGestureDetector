//
// Created by 20152 on 2023/1/7.
//
#pragma once
#include "GLBaseSample.h"

// 第二个例子，颜色由用户控制，不再写死到shader
class HelloRectangle: public GLBaseSample {
public:
    static constexpr const unsigned int VERTEX_POS_INDX = 0;
    static constexpr const unsigned int VERTEX_COLOR_INDX = 1;

    static constexpr const unsigned int VERTEX_POS_SIZE = 3;
    static constexpr const unsigned int VERTEX_COLOR_SIZE = 4;

    static constexpr const unsigned int INDICES_SIZE = 6;
public:
    HelloRectangle() = default;
    virtual ~HelloRectangle() = default;
    void Create() override;
    void Draw() override;

private:
    GLuint vboIds[2];
};
