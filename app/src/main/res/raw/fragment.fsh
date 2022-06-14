#version 300 es

precision mediump float;

uniform float iTime;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = vec4(cos(iTime) * texCoord.x, texCoord.y, sin(iTime), 1.0);
}