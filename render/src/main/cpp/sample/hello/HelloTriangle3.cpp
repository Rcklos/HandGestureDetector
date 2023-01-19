//
// Created by 20152 on 2023/1/7.
//

#include "HelloTriangle3.h"

// vertices of triangle
static GLfloat vertices[3 * HelloTriangle3::VERTEX_POS_SIZE] = {
        0.0f,   0.5f,   0.0f,
        -0.5f,  -0.5f,  0.0f,
        0.5f,   -0.5f,  0.0f
};

// color
static GLfloat color[4 * HelloTriangle3::VERTEX_COLOR_SIZE] = {
        .0f, .0f, 1.f, 1.f,
        .0f, 1.f, 0.f, 1.f,
        1.f, 0.f, 0.f, 1.f
};

static GLint vertexStrides[2] =
        {
            HelloTriangle3::VERTEX_POS_SIZE * sizeof(GLfloat),
            HelloTriangle3::VERTEX_COLOR_SIZE * sizeof(GLfloat)
        };

static GLushort indices[3] = {
        0, 1, 2
};

void HelloTriangle3::Create() {
    // 加载shader源码
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/hello_triangle2.glsl");
    FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/hello_triangle2.glsl");

    // 创建program
    m_ProgramObj = GLUtils::createProgram(&VERTEX_SHADER, &FRAGMENT_SHADER);
    if (m_ProgramObj == GL_NONE) {
        LOGE("BasicLightingSample::Init m_ProgramObj == GL_NONE")
        return;
    }

    memset(vboIds, 0, sizeof(GLuint) * 3);

    // 设置清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
}

void HelloTriangle3::Draw() {
    glClear(GL_COLOR_BUFFER_BIT);

    // 应用program
    glUseProgram(m_ProgramObj);

    if(vboIds[0] == 0 && vboIds[1] == 0 && vboIds[2] == 0) {
        // 生成三个缓冲区，分别是两个顶点属性缓冲区(坐标和颜色)、一个元素缓冲区
        glGenBuffers(3, vboIds);

        // 绑定GL_ARRAY_BUFFER缓冲区
         glBindBuffer(GL_ARRAY_BUFFER, vboIds[0]);
        // 为刚才绑定的缓冲区写入数据
        /** glBufferData是一个专门用来把用户定义的数据复制到当前绑定缓冲的函数。
         *  它的第一个参数是目标缓冲的类型：顶点缓冲对象当前绑定到GL_ARRAY_BUFFER目标上。
         *  第二个参数指定传输数据的大小(以字节为单位)；用一个简单的sizeof计算出顶点数据大小就行。
         *  第三个参数是我们希望发送的实际数据。
         *  第四个参数指定了我们希望显卡如何管理给定的数据。它有三种形式：
        GL_STATIC_DRAW ：数据不会或几乎不会改变。
        GL_DYNAMIC_DRAW：数据会被改变很多。
        GL_STREAM_DRAW ：数据每次绘制时都会改变。
        三角形的位置数据不会改变，每次渲染调用时都保持原样，所以它的使用类型最好是GL_STATIC_DRAW。
        如果，比如说一个缓冲中的数据将频繁被改变，那么使用的类型就是GL_DYNAMIC_DRAW或GL_STREAM_DRAW，
        这样就能确保显卡把数据放在能够高速写入的内存部分。
        */
        glBufferData(GL_ARRAY_BUFFER, vertexStrides[0] * 3, vertices, GL_STATIC_DRAW);

        // 重复上面的操作，对颜色进行写入
        glBindBuffer(GL_ARRAY_BUFFER, vboIds[1]);
        glBufferData(GL_ARRAY_BUFFER, vertexStrides[1] * 3, color, GL_STATIC_DRAW);

        // 写入EBO
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(GLushort) * 4, indices, GL_STATIC_DRAW);
    }

    glBindBuffer(GL_ARRAY_BUFFER, vboIds[0]);
    glEnableVertexAttribArray(VERTEX_POS_INDX);
    // 已经绑定了缓冲区，故无需再指定顶点数据的指针
    glVertexAttribPointer(VERTEX_POS_INDX, VERTEX_POS_SIZE, GL_FLOAT, GL_FALSE,
                          vertexStrides[0], nullptr);

    glBindBuffer(GL_ARRAY_BUFFER, vboIds[1]);
    glEnableVertexAttribArray(VERTEX_COLOR_INDX);
    glVertexAttribPointer(VERTEX_COLOR_INDX, VERTEX_COLOR_SIZE, GL_FLOAT, GL_FALSE,
                          vertexStrides[1], nullptr);

    // 下面这个绘制函数就可以不用了
    // glDrawArrays(GL_TRIANGLES, 0, 3);

    // 绘制元素
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
    glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_SHORT, nullptr);

    glDisableVertexAttribArray(VERTEX_POS_INDX);
    glDisableVertexAttribArray(VERTEX_COLOR_INDX);

    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_NONE);
}
