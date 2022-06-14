#version 310 es

precision mediump float;

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

in vec3 inPosition;

out vec2 texCoord;

void main() {
    gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);
    texCoord = (uSTMatrix * vec4(inPosition.xy, 0, 0)).xy;
}