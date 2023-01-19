#version 300 es
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
out vec2 vTexCoord;
uniform mat4 u_transform;
void main()
{
    gl_Position = u_transform * vec4(aPosition, 1.f);
//    vTexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);
    vTexCoord = aTexCoord;
}