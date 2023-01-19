//
// Created by 20152 on 2023/1/7.
//
#pragma once
#include "GLBaseSample.h"
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>

#define TEX_R           255, 0, 0
#define TEX_G           0, 255, 0
#define TEX_B           0, 0, 255

//#define CUBE_FRONT_LT       -0.5f,0.5f,1.f
//#define CUBE_FRONT_LB       -0.5f,-0.5f,1.f
//#define CUBE_FRONT_RB       0.5f,-0.5f, 1.f
//#define CUBE_FRONT_RT       0.5f,0.5f,1.f
#define CUBE_FRONT_LT       -0.5f,0.5f,-0.5f
#define CUBE_FRONT_LB       -0.5f,-0.5f,-0.5f
#define CUBE_FRONT_RB       0.5f,-0.5f, -0.5f
#define CUBE_FRONT_RT       0.5f,0.5f,-0.5f

#define CUBE_BACK_LT        -0.5f,0.5f,.5f
#define CUBE_BACK_LB        -0.5f,-0.5f,.5f
#define CUBE_BACK_RB        0.5f,-0.5f, .5f
#define CUBE_BACK_RT        0.5f,0.5f,.5f

#

// 第二个例子，颜色由用户控制，不再写死到shader
class HelloCube2: public GLBaseSample {
public:
    static constexpr const unsigned int VERTEX_POS_INDX = 0;
    static constexpr const unsigned int VERTEX_POS_SIZE = 3;

    static constexpr const unsigned int VERTEX_TEX_INDX = 1;
    static constexpr const unsigned int VERTEX_TEX_SIZE = 2;

    static constexpr const unsigned FACE_SIZE = 6;
    static constexpr const unsigned int INDICES_SIZE = FACE_SIZE * 6;
public:
    HelloCube2() = default;
    virtual ~HelloCube2() = default;
    void Create() override;
    void Draw() override;

private:
    GLuint vboIds[3];

    GLuint samplerLoc;
    GLuint textureId;
    static GLuint CreateRectangleTexture2D();

    // model变换矩阵
    glm::mat4 model;
    GLuint modelLoc;

    // view变换矩阵
    glm::mat4 view;
    GLuint viewLoc;

    // projection变换矩阵
    glm::mat4 proj;
    GLuint projLoc;

    float computeDeltaTime();
    void update(float deltaTime);
    long mLastTime = 0l;
    float degree = 0.f;
    float fovy = 45.f;
    float viewTranslate = -10.f;
};
