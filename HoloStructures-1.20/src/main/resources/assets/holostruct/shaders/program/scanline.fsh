#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform float Alpha;
uniform float GameTime;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	
	bool line = mod(gl_FragCoord.y +(GameTime * -2), 10) >= 5;
	
	vec4 lineColor = vec4(1, 1, 1, 1);
	if (line) lineColor = vec4(0.5F, 0.5F, 0.5F, Alpha);
	
	gl_FragDepth = texture(DepthSampler, texCoord).x;
    fragColor = texture(DiffuseSampler, texCoord) * lineColor;
}
