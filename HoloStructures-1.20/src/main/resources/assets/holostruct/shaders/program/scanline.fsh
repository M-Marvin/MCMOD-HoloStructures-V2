#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform float GameTime;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	
	float depthB = texture(DepthSampler, texCoord).x;
	
	bool line = mod(gl_FragCoord.y +(GameTime * -2), 10) >= 5;
	
	vec4 lineColor = vec4(1, 1, 1, 1);
	if (line) lineColor = vec4(0.5F, 0.5F, 0.5F, 1);
	
	gl_FragDepth = depthB - (0.00001); // TODO slight offset in the depthbuffer, to prevent z fighting
    fragColor = texture(DiffuseSampler, texCoord) * lineColor;
}
