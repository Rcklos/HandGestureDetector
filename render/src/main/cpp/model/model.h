//
// Created by 20152 on 2023/1/9.
//
#pragma once
#include "mesh.h"

// 包含Assimp对应的头文件
#include <assimp/Importer.hpp>
#include <assimp/scene.h>
#include <assimp/postprocess.h>

// 导入opencv
#include <opencv2/opencv.hpp>

using namespace std;

class Model {
public:
    // 模型包含的全部网格
    vector<Mesh> meshes;
    string directory;

    // 贴图缓存
    vector<Texture> textures_loaded;

    Model(string const &path): hasTexture(false)
    {
        loadModel(path);
    }

    // 循环绘制mesh
    void Draw(Shader shader)
    {
        for_each(meshes.begin(), meshes.end(), [&](Mesh &mesh){
            mesh.Draw(shader);
        });
    }

    bool ContainsTextures()
    {
        return hasTexture;
    }

    void Destroy()
    {
        for_each(meshes.begin(), meshes.end(), [&](Mesh &mesh) {
            mesh.Destroy();
        });
    }

private:
    bool hasTexture;
    void loadModel(string const &path)
    {
        Assimp::Importer importer;
        // 导入模型
        LOGD("load 3d model object from path: %s", path.c_str());
        const aiScene *scene = importer.ReadFile(path,
                                                 aiProcess_Triangulate | aiProcess_FlipUVs);

        if(!scene || scene->mFlags & AI_SCENE_FLAGS_INCOMPLETE || !scene->mRootNode) {
            LOGE("Model::loadModel path=%s, assimpError=%s", path.c_str(),
                 importer.GetErrorString());
            return;
        }
        DEBUG_LOGCATE()

        // 保存文件夹路径, 用于读取纹理文件
        directory = path.substr(0, path.find_last_of('/'));

        processNode(scene->mRootNode, scene);
    }

    void processNode(aiNode *node, const aiScene *scene)
    {
        DEBUG_LOGCATE()
        // 遍历处理节点中所有的网格
        for(unsigned i = 0; i < node->mNumMeshes; i++) {
            aiMesh *mesh = scene->mMeshes[node->mMeshes[i]];
            if(mesh)
                meshes.push_back(processMesh(mesh, scene));
        }
        DEBUG_LOGCATE()

        // 遍历处理节点
        for(unsigned  i = 0; i < node->mNumChildren; i++)
            processNode(node->mChildren[i], scene);
    }

    Mesh processMesh(aiMesh *mesh, const aiScene *scene)
    {
        vector<Vertex> vertices;
        vector<unsigned> indices;
        vector<Texture> textures;

        // 遍历将mesh中的顶点转换成glm可以处理的格式
        for(unsigned i = 0; i < mesh->mNumVertices; i++) {
            Vertex vertex;
            glm::vec3 vector;

            // 转换顶点坐标
            vector.x = mesh->mVertices[i].x;
            vector.y = mesh->mVertices[i].y;
            vector.z = mesh->mVertices[i].z;
            vertex.Position = vector;

            // 转换纹理坐标
            // 只接受第一个纹理坐标，别的不例会哦
            if(mesh->mTextureCoords[0])
            {
                LOGD("Model::processMesh: load texture");
                glm::vec2 vec;
                vec.x = mesh->mTextureCoords[0][i].x;
                vec.y = mesh->mTextureCoords[0][i].y;
                vertex.TexCoords = vec;
            }
            else vertex.TexCoords = glm::vec2(0.f, 0.f);
            vertices.push_back(vertex);
        }

        // 生成索引
        // 遍历face，每一个face都包含着indices
        for(unsigned i = 0; i < mesh->mNumFaces; i++) {
            aiFace face = mesh->mFaces[i];
            for(unsigned j = 0; j < face.mNumIndices; j++)
                indices.push_back(face.mIndices[j]);
        }

        // 处理材质
        /**
         * TODO: 处理所有类型的材质
         * 目前暂时只接收漫反射的材质
         */
        if(mesh->mMaterialIndex >= 0) {
            aiMaterial *material = scene->mMaterials[mesh->mMaterialIndex];
            vector<Texture> diffuseMaps = loadMaterialTexture(material, aiTextureType_DIFFUSE,
                                                              "texture_diffuse");
            textures.insert(textures.end(), diffuseMaps.begin(), diffuseMaps.end());
        }

        return Mesh(vertices, indices, textures);
    }

    vector<Texture> loadMaterialTexture(aiMaterial *mat, aiTextureType type, string typeName)
    {
        DEBUG_LOGCATE()
        vector<Texture> textures;
        LOGD("loadMaterialTexture: texture count = %d", mat->GetTextureCount(type))
        for(unsigned i = 0; i < mat->GetTextureCount(type); i++) {
            aiString str;
            mat->GetTexture(type, i, &str);

            /**
             * TODO: 解决纹理重复加载的问题
             */
            bool skip = false;

            for(unsigned int j = 0; j < textures_loaded.size(); j++) {
                if(std::strcmp(textures_loaded[j].path.data(), str.C_Str()) == 0) {
                    textures.push_back(textures_loaded[j]);
                    skip = true;
                    break;
                }
            }

            if(!skip) {
                Texture texture;
                texture.id = TextureFromFile(str.C_Str(), this->directory);
                texture.type = typeName;
                texture.path = str.C_Str();
                textures.push_back(texture);
                textures_loaded.push_back(texture);
            }

        }
        return textures;
    }

    // 将文件加载成texture，使用opencv读取然后传入Opengl ES
    unsigned int TextureFromFile(const char *path, const string &dir, bool gamma = false) {
        string filename = string(path);
        filename = dir + '/' + filename;

        unsigned int textureID;
        glGenTextures(1, &textureID);

        int width, height, nrComponents;
        unsigned char *data = nullptr;

        // load the texture using OpenCV
        LOGD("TextureFromFile Loading texture %s", filename.c_str())

        // 使用 OpenCV 对图片进行解码
        cv::Mat textureImage = cv::imread(filename);
        if (!textureImage.empty()) {
            hasTexture = true;
            // OpenCV 默认解码成 BGR 格式，这里转换为 RGB
            // opencv reads textures in BGR format, change to RGB for GL
            cv::cvtColor(textureImage, textureImage, CV_BGR2RGB);
            // opencv reads image from top-left, while GL expects it from bottom-left
            // vertically flip the image
            //cv::flip(textureImage, textureImage, 0);

            glBindTexture(GL_TEXTURE_2D, textureID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, textureImage.cols,
                         textureImage.rows, 0, GL_RGB, GL_UNSIGNED_BYTE,
                         textureImage.data);
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GO_CHECK_GL_ERROR()
        } else {
            LOGD("TextureFromFile Texture failed to load at path: %s", path)
        }

        return textureID;
    }
};