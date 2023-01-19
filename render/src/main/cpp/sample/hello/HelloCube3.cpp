//
// Created by 20152 on 2023/1/7.
//

#include "HelloCube3.h"

static GLfloat vertices[4 * HelloCube3::FACE_SIZE * HelloCube3::VERTEX_POS_SIZE] = {
        // 前面正视
        CUBE_FRONT_LB,      // 左下
        CUBE_FRONT_RB,      // 右下
        CUBE_FRONT_RT,      // 右上
        CUBE_FRONT_LT,      // 左上

        // 背面后视
        CUBE_BACK_RB,       // 左下
        CUBE_BACK_LB,       // 右下
        CUBE_BACK_LT,       // 右上
        CUBE_BACK_RT,       // 左上

        // 顶面俯视
        CUBE_FRONT_LT,
        CUBE_FRONT_RT,
        CUBE_BACK_RT,
        CUBE_BACK_LT,

        // 底面仰视
        CUBE_BACK_LB,
        CUBE_BACK_RB,
        CUBE_FRONT_RB,
        CUBE_FRONT_LB,

        // 左视
        CUBE_BACK_LB,
        CUBE_FRONT_LB,
        CUBE_FRONT_LT,
        CUBE_BACK_LT,

        // 右视
        CUBE_FRONT_RB,
        CUBE_BACK_RB,
        CUBE_BACK_RT,
        CUBE_FRONT_RT
};

static GLfloat texes[4 * HelloCube3::FACE_SIZE * HelloCube3::VERTEX_TEX_SIZE] = {
        // 前面
        0, 0,
        1,0,
        1, 1,
        0,1,

        // 背面
        0, 0,
        1,0,
        1, 1,
        0,1,

        // 顶面
        0, 0,
        1,0,
        1, 1,
        0,1,

        // 底面
        0, 0,
        1,0,
        1, 1,
        0,1,

        // 左视
        0, 0,
        1,0,
        1, 1,
        0,1,

        // 右视
        0, 0,
        1,0,
        1, 1,
        0,1,
};

static GLint vertexStride = (HelloCube3::VERTEX_POS_SIZE) * sizeof(GLfloat);

static GLushort indices[HelloCube3::INDICES_SIZE];

void HelloCube3::Create() {
    // 加载shader源码
    VERTEX_SHADER = GLUtils::openTextFile("shader/vertex/hello_cube3.glsl");
    FRAGMENT_SHADER = GLUtils::openTextFile("shader/fragment/hello_rectangle3.glsl");

    // 创建program
    m_ProgramObj = GLUtils::createProgram(&VERTEX_SHADER, &FRAGMENT_SHADER);
    if (m_ProgramObj == GL_NONE) {
        LOGE("BasicLightingSample2::Init m_ProgramObj == GL_NONE")
        return;
    }

    GLfloat template_[6] = {
            0, 1, 2,
            0, 2, 3
    };
    for(int i = 0; i < FACE_SIZE; i++) {
        for(int j = 0; j < 6; j++) {
            indices[i * 6 + j] = i * 4 + template_[j];
        }
    }

    samplerLoc = glGetUniformLocation(m_ProgramObj, "s_texture");
    textureId = CreateRectangleTexture2D();

    transformLoc = glGetUniformLocation(m_ProgramObj, "u_transform");

    // 生成三个缓冲区，分别是两个顶点属性缓冲区(坐标和颜色)、一个元素缓冲区
    glGenBuffers(3, vboIds);

    // 绑定GL_ARRAY_BUFFER缓冲区
    glBindBuffer(GL_ARRAY_BUFFER, vboIds[0]);
    // 为刚才绑定的缓冲区写入数据
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER, vboIds[1]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(texes), texes, GL_STATIC_DRAW);

    // 写入EBO
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

    // 设置清除颜色
    glClearColor(1.f, 1.f, 1.f, 1.f);
    glEnable(GL_DEPTH_TEST);
}

void HelloCube3::Draw() {
    update(computeDeltaTime());
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

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

    // 变换
    glUniformMatrix4fv(transformLoc, 1, GL_FALSE, glm::value_ptr(trans));

    // 绘制元素
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
    glDrawElements(GL_TRIANGLES, INDICES_SIZE, GL_UNSIGNED_SHORT, nullptr);

    glDisableVertexAttribArray(VERTEX_POS_INDX);
    glDisableVertexAttribArray(VERTEX_TEX_INDX);

    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_NONE);
}

GLuint HelloCube3::CreateRectangleTexture2D() {
    GLuint id = GLUtils::loadTexture("texture/basemap.png");
    if(id == -1) {
        LOGE("Error loading texture file from assets");
    }
    LOGI("load texture id is %u", id)
    return id;
}

float HelloCube3::computeDeltaTime() {
    if (mLastTime == 0) {
        mLastTime = TimeUtils::currentTimeMillis();
    }
    long currentTime = TimeUtils::currentTimeMillis();
    long elapsedTime = currentTime - mLastTime;
    float deltaTime = (float) elapsedTime / 1000.0f;
    mLastTime = currentTime;
    return deltaTime;
}

void HelloCube3::update(float deltaTime) {
    if(degree < 360.f) degree += 15.f * deltaTime;
    else degree = 0;

    auto aspect = (GLfloat) m_Width / (GLfloat) m_Height;
    auto scale = .7f;

    /**
     * mvp变换顺序:
     * 1. 先对模型(model)进行变换，主要有缩放、平移、旋转
     * 2. 摆好摄像机的位置
     * 3. 投影成像
     * mvp的变换顺序实际上对应着三个坐标系，即三种空间：
     * 0. 局部空间(顶点位置定义，不在变换空间讨论范围内)
     * 1. 世界空间
     * 2. 观察空间
     * 3. 裁剪空间
     * 进行矩阵变换时，需要考虑到顺序: 先平移，在旋转，最后缩放
     * 也就是和所谓的[先缩放再旋转再平移]的操作是反过来的
     * 原因是书写时矩阵是矩阵行主序的，而OpenGL ES中又是列主序
     * 不管行列主序，在数学意义上操作是全等的
     * 只是实际操作起来的时候，需要倒过来，例如mvp变换就变成了[pvm]
     */
    glm::mat4 proj = glm::perspective(glm::radians(45.f), aspect, 1.0f, 20.f);
    auto model = glm::mat4(1.f);
    // 位移到视野看得到的地方
    model = glm::translate(model, glm::vec3(0.f, 0.f, -3.f));
    // 旋转立方体
    model = glm::rotate(model, glm::radians(degree), glm::vec3(-1.f, -1.f, 0.f));
    // 缩放
    model = glm::scale(model, glm::vec3(scale, scale, scale));
    trans = proj * model;
}
