//
// Created by 20152 on 2023/1/9.
//
#pragma once
#include <shader.h>
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>
#include <glm/gtc/type_ptr.hpp>

#include <string>
#include <vector>
using namespace std;

#define MESH_VERTEX_POSITION_INDEX      0
#define MESH_TEXTURE_COORDS_INDEX       1

// 定点属性
struct Vertex {
    // 顶点坐标
    glm::vec3 Position;
    // 贴图坐标
    glm::vec2 TexCoords;
};

// 纹理
struct Texture {
    unsigned id;
    string type;
    // 贴图路径
    string path;
};

class Mesh {
public:
    // 顶点
    vector<Vertex> vertices;
    // 顶点索引
    vector<unsigned> indices;
    // 纹理
    vector<Texture> textures;


    Mesh(vector<Vertex> vertices, vector<unsigned> indices, vector<Texture> textures):
            vertices(vertices), indices(indices), textures(textures)
    {
        LOGD("Mesh: vertices size = %ld, indices size = %ld, textures size = %ld",
             vertices.size(), indices.size(), textures.size());
        setupMesh();
    }

    // 渲染
    void Draw(Shader shader)
    {
        if(textures.size() > 0) {
            /**
             * TODO 实现各种纹理加载
             * 实际上，如果你能够看到这条注释，意味着此时我还未实现完这个代码
             * 下面的代码并不能实现各种纹理的加载且只能加载一个纹理
             */
            glActiveTexture(GL_TEXTURE0);
            GLuint diffuse = glGetUniformLocation(shader.ID, "texture_diffuse1");
            glUniform1i(diffuse, 0);
            glBindTexture(GL_TEXTURE_2D, textures[0].id);
        }

        // 绘制网格
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(GL_NONE);

        glActiveTexture(GL_TEXTURE0);
    }

    void Destroy()
    {
        for(int i = 0; i < textures.size(); i++)
            glDeleteTextures(1, &textures[i].id);
        glDeleteBuffers(1, &EBO);
        glDeleteBuffers(1, &VBO);
        glDeleteBuffers(1, &VAO);
        VAO = VBO = EBO = GL_NONE;
    }

private:
    unsigned VAO, VBO, EBO;
    // 初始化mesh
    void setupMesh()
    {
        // 生成缓冲区
        glGenVertexArrays(1, &VAO);
        glGenVertexArrays(1, &VBO);
        glGenVertexArrays(1, &EBO);

        /**
         * 初始化缓冲区
         */
        // 绑定VAO，记录VBO和EBO的操作
        glBindVertexArray(VAO);

        // 将顶点属性写入VBO
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        // vector内部由数组实现，故可以直接&vertices[0]获取数组指针
        glBufferData(GL_ARRAY_BUFFER, vertices.size() * sizeof(Vertex),
                     &vertices[0], GL_STATIC_DRAW);

        // 写入顶点索引
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(unsigned),
                     &indices[0], GL_STATIC_DRAW);

        glEnableVertexAttribArray(MESH_VERTEX_POSITION_INDEX);
        glVertexAttribPointer(MESH_VERTEX_POSITION_INDEX, 3, GL_FLOAT,
                              GL_FALSE,sizeof(Vertex), (void *)0);

        glEnableVertexAttribArray(MESH_TEXTURE_COORDS_INDEX);
        // offsetof关键字可以获取结构体中某个属性的地址偏移量
        glVertexAttribPointer(MESH_TEXTURE_COORDS_INDEX, 2, GL_FLOAT,
                              GL_FALSE, sizeof(Vertex),
                              (void *) offsetof(Vertex, TexCoords));

        glBindVertexArray(GL_NONE);
    }
};