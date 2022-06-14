#version 310 es
#extension GL_EXT_texture_buffer : enable

precision mediump float;

uniform sampler2D scheme;
uniform float spectrum[7];

in vec2 texCoord;
out vec4 fragColor;

void main() {
    int index = int(texCoord.x * 6.9999);
    float spec = spectrum[index];
    vec3 black = vec3(0.0, 0.0, 0.0);
    vec3 col = texture(scheme, texCoord).rgb;
    fragColor = vec4(mix(col, black, step(texCoord.y, spec)), 1.0);
}