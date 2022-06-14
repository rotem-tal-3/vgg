#version 310 es

#define PI 3.141596

precision mediump float;

uniform sampler2D scheme;
uniform float spectrum[7];
uniform float iTime;
uniform float aspectRatio;

in vec2 texCoord;
out vec4 fragColor;

vec3 a = vec3(0.5, 0.5, 0.5);
vec3 b = vec3(0.5, 0.5, 0.5);
vec3 c = vec3(1.0, 1.0, 1.0);
vec3 d = vec3(0.00, 0.33, 0.67);

// iq color mapper
vec3 colorMap(float t) {
	return (a + b * cos(2. * PI * (c * t + d)));
}

void main()
{
    vec2 uv = texCoord.yx;
    uv -= 0.5;
    uv.x *= aspectRatio;
    float bass = (spectrum[1] + spectrum[2]) / 2.0;
    float mid = (spectrum[3] + spectrum[4]) / 2.0 ;
    vec3 s3 = texture(scheme, vec2((1. + sin(iTime)) /  2., 0.01)).rgb;

    float r = length(uv);
    float bril = spectrum[5] * r * 22.;
    float a = atan(uv.y, uv.x);

    float ring = 1.5 + 0.8 * sin(PI * 0.25 * iTime);

    float kr = bass * 1.7 - 0.5 * cos(7. * PI * r);
    vec3 kq = 0.5 - 0.5 * s3*sin(ring*vec3(30., 29.3, 28.6) * r - 6.0 * iTime + PI * vec3(-0.05, 0.5, 1.0));
    vec3 c = kr * (0.1 + kq * (1. - 0.5* colorMap(a / PI))) * (0.5 + 0.5 * sin(12.*a*mid + bril));

    fragColor = vec4(mix(vec3(0.0, 0.0, 0.2), c, 0.85), 1.0);
}