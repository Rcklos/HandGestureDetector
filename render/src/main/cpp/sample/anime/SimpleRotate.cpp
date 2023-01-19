//
// Created by 20152 on 2023/1/7.
//

#include "SimpleRotate.h"


// 顶点数据
static GLfloat vertices[3 * 3] = {
        0.0f,   0.5f,   0.0f,
        -0.5f,  -0.5f,  0.0f,
        0.5f,   -0.5f,  0.0f
};

void SimpleRotate::Create() {
    // 加载shader源码
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/simple_transform.glsl");
    FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/hello_triangle.glsl");

    // 创建program
    m_ProgramObj = GLUtils::createProgram(&VERTEX_SHADER, &FRAGMENT_SHADER);
    if (m_ProgramObj == GL_NONE) {
        LOGE("BasicLightingSample::Init m_ProgramObj == GL_NONE")
        return;
    }

    transformLoc = glGetUniformLocation(m_ProgramObj, "u_transform");

    // 设置清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
}

void SimpleRotate::update(float deltaTime) {
    if(degree < 360.f) degree += 10.f * deltaTime;
    else degree = 0;

    trans = glm::rotate(glm::mat4(1.0f), glm::radians(degree), glm::vec3(0.f, 0.f, 1.f));
    auto value = glm::value_ptr(trans);
//    for(int i = 0; i < 4; i++)
//            LOGI("%.2f, %.2f, %.2f, %.2f",
//                 value[i * 4], value[i * 4 + 1], value[i * 4 + 2], value[i * 4 + 3]);
}

void SimpleRotate::Draw() {
    update(computeDeltaTime());
    // 设置视图
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(m_ProgramObj);

    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(VERTEX_POS_INDX);

    glUniformMatrix4fv(transformLoc, 1, GL_FALSE, glm::value_ptr(trans));

    glDrawArrays(GL_TRIANGLES, 0, 3);
}

float SimpleRotate::computeDeltaTime() {
    if (mLastTime == 0) {
        mLastTime = TimeUtils::currentTimeMillis();
    }
    long currentTime = TimeUtils::currentTimeMillis();
    long elapsedTime = currentTime - mLastTime;
    float deltaTime = (float) elapsedTime / 1000.0f;
    mLastTime = currentTime;
    return deltaTime;
}

