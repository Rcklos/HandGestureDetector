//
// Created by 20152 on 2023/1/7.
//

#include "HelloRectangle3.h"

// vertices of triangle
static GLfloat vertices[4 * HelloRectangle3::VERTEX_POS_SIZE] = {
        -1.0f, -0.5f, 0.0f,         // 左下
        1.0f, -0.5f, 0.0f,          // 右下
        1.0f, 0.5f, 0.0f,           // 右上
        -1.0f, 0.5f, 0.0f,        // 左上
};

static GLfloat texes[4 * HelloRectangle3::VERTEX_TEX_SIZE] = {
        0.f, 0.f,
        1.f, 0.f,
        1.f, 1.f,
        0.f, 1.f
};

static GLint vertexStride = (HelloRectangle3::VERTEX_POS_SIZE) * sizeof(GLfloat);

static GLushort indices[HelloRectangle3::INDICES_SIZE] = {
        0, 1, 2,
        0, 2, 3
};

void HelloRectangle3::Create() {
    // 加载shader源码
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/hello_rectangle3.glsl");
    FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/hello_rectangle3.glsl");

    // 创建program
    m_ProgramObj = GLUtils::createProgram(&VERTEX_SHADER, &FRAGMENT_SHADER);
    if (m_ProgramObj == GL_NONE) {
        LOGE("BasicLightingSample2::Init m_ProgramObj == GL_NONE")
        return;
    }

    samplerLoc = glGetUniformLocation(m_ProgramObj, "s_texture");
    textureId = CreateRectangleTexture2D();

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
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER, vboIds[1]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(texes), texes, GL_STATIC_DRAW);

    // 写入EBO
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

    // 设置清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
}

void HelloRectangle3::Draw() {
    glClear(GL_COLOR_BUFFER_BIT);

    // 应用program
    glUseProgram(m_ProgramObj);

    glBindBuffer(GL_ARRAY_BUFFER, vboIds[0]);
    // 已经绑定了缓冲区，故无需再指定顶点数据的指针
    glVertexAttribPointer(VERTEX_POS_INDX, VERTEX_POS_SIZE, GL_FLOAT, GL_FALSE,
                          vertexStride, nullptr);
    // 贴图
    glBindBuffer(GL_ARRAY_BUFFER, vboIds[1]);
    glVertexAttribPointer(VERTEX_TEX_INDX, VERTEX_TEX_SIZE, GL_FLOAT, GL_FALSE,
                          VERTEX_TEX_SIZE * sizeof(GLfloat), nullptr);

    glEnableVertexAttribArray(VERTEX_POS_INDX);
    glEnableVertexAttribArray(VERTEX_TEX_INDX);

    // 激活纹理
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glUniform1i(samplerLoc, 0);

    // 绘制元素
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
    glDrawElements(GL_TRIANGLES, INDICES_SIZE, GL_UNSIGNED_SHORT, nullptr);

    glDisableVertexAttribArray(VERTEX_POS_INDX);
    glDisableVertexAttribArray(VERTEX_TEX_INDX);

    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_NONE);
}

GLuint HelloRectangle3::CreateRectangleTexture2D() {
    GLuint rectTextureId;
    constexpr GLuint width = 3;
    constexpr GLuint height = 3;

    GLubyte pixels[width * height * 3] =
            {
                TEX_R,      // 0.0, 0.0
                TEX_G,      // 0.5, 0.0
                TEX_B,      // 1.0, 0.0

                TEX_G,      // 0.0, 0.5
                TEX_B,      // 0.5, 0.5
                TEX_R,      // 1.0, 0.5

                TEX_B,      // 0.0, 1.0
                TEX_R,      // 0.5, 1.0
                TEX_G       // 1.0, 1.0
            };

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glGenTextures(1, &rectTextureId);
    glBindTexture(GL_TEXTURE_2D, rectTextureId);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB,
                 width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, pixels);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    return rectTextureId;
}
