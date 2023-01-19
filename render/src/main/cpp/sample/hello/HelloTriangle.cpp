//
// Created by 20152 on 2023/1/7.
//

#include "HelloTriangle.h"

void HelloTriangle::Create() {
    // 加载shader源码
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/hello_triangle.glsl");
    FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/hello_triangle.glsl");

    // 创建program
    m_ProgramObj = GLUtils::createProgram(&VERTEX_SHADER, &FRAGMENT_SHADER);
    if (m_ProgramObj == GL_NONE) {
        LOGE("BasicLightingSample::Init m_ProgramObj == GL_NONE")
        return;
    }

    // 设置清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
}

void HelloTriangle::Draw() {
// 顶点数据
    GLfloat vertices[] = {
            0.0f,   0.5f,   0.0f,
            -0.5f,  -0.5f,  0.0f,
            0.5f,   -0.5f,  0.0f
    };

    // 设置视图
    glViewport(0, 0, m_Width, m_Height);
    glClear(GL_COLOR_BUFFER_BIT);

    // 应用program
    glUseProgram(m_ProgramObj);

    // 设置顶点属性指针，glVertexAttribPointer参数如下:
    // 1. 需要设置的顶点属性索引，注意这里的索引指的是我们需要配置的顶点属性的索引，而不是vertices(顶点数据)的索引
    // 2. 顶点属性类型，也就是顶点属性的维度，这里xyz三维则填3
    // 3. 是否需要标准化(归一化)，如果归一化，所有数据都会被设置到0~1之间(有符号型是-1~1)
    // 4. 步长，由于顶点数据在数组中相邻的都是直接挨着的，所以设0
    // 5. 顶点数据指针
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, vertices);
    // 启用顶点属性(默认是禁用)，其传入的参数值应是顶点属性对应的location
    glEnableVertexAttribArray(VERTEX_POS_INDX);

    glDrawArrays(GL_TRIANGLES, 0, 3);
}
