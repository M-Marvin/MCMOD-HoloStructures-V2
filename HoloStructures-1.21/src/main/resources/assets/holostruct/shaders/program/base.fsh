#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main(){
    gl_FragDepth = texture(DepthSampler, texCoord).x;
    fragColor = texture(DiffuseSampler, texCoord);
}
