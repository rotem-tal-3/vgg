#version 310 es

precision mediump float;

uniform sampler2D scheme;
uniform float spectrum[7];

in vec2 texCoord;
out vec4 fragColor;

vec3 rgb_to_yiq(vec3 rgb) {
    float y = 0.30*rgb.x + 0.59*rgb.y + 0.11*rgb.z;
    return vec3(y, -0.27*(rgb.z-y) + 0.74*(rgb.x-y),0.41*(rgb.z-y) + 0.48*(rgb.x-y));
}

void main() {
    int index = int(texCoord.x * 6.9999);
    float spec = spectrum[index];
    vec4 col = texture(scheme, texCoord);
    vec3 yiq_col = rgb_to_yiq(col.rgb);

    fragColor = mix(col, vec4(1.0 - yiq_col, 0.9), step(texCoord.y, spec));
}