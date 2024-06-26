#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec3 Gray;
uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;
uniform vec3 Offset;
uniform vec3 ColorScale;
uniform float Saturation;

out vec4 fragColor;

void main() {
    vec4 InTexel = texture(DiffuseSampler, texCoord);
	float InDepth = texture(DepthSampler, texCoord).r;
	
    // Color Matrix
    float RedValue = dot(InTexel.rgb, RedMatrix);
    float GreenValue = dot(InTexel.rgb, GreenMatrix);
    float BlueValue = dot(InTexel.rgb, BlueMatrix);
    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);
	
    // Offset & Scale
    OutColor = (OutColor * ColorScale) + Offset;
	
    gl_FragDepth = InDepth;
    fragColor = vec4(OutColor, InTexel.a);
}
