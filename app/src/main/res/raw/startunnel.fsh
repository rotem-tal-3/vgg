#version 300 es

uniform float iTime;
//uniform sampler2D iSound;

in vec2 texCoord;
out vec4 fragColor;

float pn( vec3 x ) // iq noise
{
    return -1.0+2.4*0.6;
}

vec2 path(float t)
{
    return vec2(cos(t*0.2), sin(t*0.2)) * 2.;
}

const mat3 mx = mat3(1,0,0,0,7,0,0,0,7);
const mat3 my = mat3(7,0,0,0,1,0,0,0,7);
const mat3 mz = mat3(7,0,0,0,7,0,0,0,1);

// base on shane tech in shader : One Tweet Cellular Pattern
float func(vec3 p)
{
    p = fract(p/68.6) - .5;
    return min(min(abs(p.x), abs(p.y)), abs(p.z)) + 0.1;
}

vec3 effect(vec3 p)
{
    p *= mz * mx * my * sin(p.zxy); // sin(p.zxy) is based on iq tech from shader (Sculpture III)
    return vec3(min(min(func(p*mx), func(p*my)), func(p*mz))/.6);
}

vec4 displacement(vec3 p)
{
    vec3 col = 1.-effect(p*0.8);
    col = clamp(col, -.5, 1.);
    float dist = dot(col,vec3(0.023));
    col = step(col, vec3(0.82));// black line on shape
    return vec4(dist,col);
}

vec4 map(vec3 p)
{
    p.xy -= path(p.z);
    vec4 disp = displacement(sin(p.zxy*2.)*0.8);
    p += sin(p.zxy*.5)*1.5;
    float l = length(p.xy) - 4.;
    return vec4(max(-l + 0.09, l) - disp.x, disp.yzw);
}

vec3 nor(vec3 pos, float prec )
{
    vec3 eps = vec3( prec, 0., 0. );
    vec3 nor = vec3(
    map(pos+eps.xyy).x - map(pos-eps.xyy).x,
    map(pos+eps.yxy).x - map(pos-eps.yxy).x,
    map(pos+eps.yyx).x - map(pos-eps.yyx).x );
    return normalize(nor);
}


vec4 light(vec3 ro, vec3 rd, float d, vec3 lightpos, vec3 lc)
{
    vec3 p = ro + rd * d;

    // original normale
    vec3 n = nor(p, 0.1);

    vec3 lightdir = lightpos - p;
    float lightlen = length(lightpos - p);
    lightdir /= lightlen;

    float amb = 0.6;
    float diff = clamp( dot( n, lightdir ), 0.0, 1.0 );

    vec3 brdf = vec3(0);
    brdf += amb * vec3(0.2,0.5,0.3); // color mat
    brdf += diff * 0.6;

    brdf = mix(brdf, map(p).yzw, 0.5);// merge light and black line pattern

    return vec4(brdf, lightlen);
}

vec3 stars(vec2 uv, vec3 rd, float d,vec2 g)
{
    float k = fract( cos(uv.y * 0.0001 + uv.x) * 90000.);
    float var = sin(pn(d*0.6+rd*182.14))*0.5+0.5;// thank to klems for the variation in my shader subluminic
    vec3 col = vec3(mix(0., 1., var*pow(k, 200.)));// come from CBS Shader "Simplicity" : https://www.shadertoy.com/view/MslGWN
    return col;
}

void main() {
    float time = iTime*1.;
    float cam_a = time; // angle z

    float cam_e = 3.2; // elevation
    float cam_d = 4.; // distance to origin axis

    float maxd = 40.; // ray marching distance max

    vec2 uv = texCoord;

    vec3 col = vec3(0.);

    vec3 ro = vec3(path(time),time);
    vec3 cv = vec3(path(time+0.1),time+0.1);

    vec3 cu=vec3(0,1,0);
    vec3 rov = normalize(cv-ro);
    vec3 u = normalize(cross(cu,rov));
    vec3 v = cross(rov,u);
    vec3 rd = normalize(rov + uv.x*u + uv.y*v);

    vec3 curve0 = vec3(0);
    vec3 curve1 = vec3(0);
    vec3 curve2 = vec3(0);
    float ao = 0.; // ao low cost :)

    float st = 0.;
    float d = 0.;
    for(int i=0;i<250;i++)
    {
        if (st<0.025*log(d*d/st/1e5)||d>maxd) break;// special break condition for low thickness object
        st = map(ro+rd*d).x;
        d += st * 0.6; // the 0.6 is selected according to the 1e5 and the 0.025 of the break condition for good result
        ao++;

    }
    if (d < maxd) {
        vec4 li = light(ro, rd, d, ro, vec3(0));// point light on the cam
        col = li.xyz/(li.w*0.2);// cheap light attenuation

        col = mix(vec3(1.-ao/100.), col, 0.5);// low cost ao :)
        fragColor.rgb = mix(col, vec3(0), 1.0-exp(-0.003*d*d));
    } else {
        fragColor.rgb = stars(uv, rd, d, texCoord);// stars bg
    }

    // vignette
    vec2 q = texCoord;
    fragColor.rgb *= 0.5 + 0.5*pow( 16.0*q.x*q.y*(1.0-q.x)*(1.0-q.y), 0.25 ); // iq vignette

}
