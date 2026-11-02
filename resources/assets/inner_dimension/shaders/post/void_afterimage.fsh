#version 330
uniform sampler2D InSampler;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
in vec2 texCoord;
out vec4 fragColor;
float voidMask(vec3 c) {
    float dark=1.0-smoothstep(0.10,0.30,max(c.r,max(c.g,c.b)));
    float purple=smoothstep(0.015,0.13,(c.r+c.b)*0.5-c.g);
    return max(dark,purple*(1.0-smoothstep(0.25,0.55,c.g)));
}
void main() {
    vec4 original=texture(InSampler,texCoord);
    vec2 pixel=1.0/InSize;
    vec3 result=original.rgb;
    for(int i=1;i<=3;i++) {
        vec3 ghost=texture(InSampler,clamp(texCoord-vec2(float(i)*2.0,float(i)*0.35)*pixel,vec2(0.0),vec2(1.0))).rgb;
        float edge=abs(voidMask(ghost)-voidMask(original.rgb));
        result=mix(result,ghost*vec3(1.04,0.94,1.08),edge*0.045/float(i));
    }
    fragColor=vec4(result,original.a);
}
