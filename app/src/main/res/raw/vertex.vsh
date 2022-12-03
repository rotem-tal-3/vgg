#version 300 es

precision mediump float;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform mat4 uSTMatrix;

in vec3 inPosition;
in vec2 inTexCoord;

out vec2 texCoord;

void main() {
    gl_Position = projection * view * model * vec4(inPosition.xyz, 1);
    texCoord = (uSTMatrix * vec4(inPosition.xy, 0, 0)).xy;
}