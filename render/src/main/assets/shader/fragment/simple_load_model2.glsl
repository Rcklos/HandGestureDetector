#version 300 es
precision mediump float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D texture_diffuse1;
void main()
{
    fragColor = texture(texture_diffuse1, vTexCoord);
}