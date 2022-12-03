#version 310 es

precision mediump float;

uniform samplerCube sky;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 coords = gl_Position.xyz + texCoord.x * 0.0;
    fragColor = mix(texture(sky, coords), gl_FragColor, gl_FragColor.a);
}