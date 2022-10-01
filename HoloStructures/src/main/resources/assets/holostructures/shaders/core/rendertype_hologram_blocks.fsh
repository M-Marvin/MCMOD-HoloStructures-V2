#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
	
	bool line = mod(gl_FragCoord.y + (GameTime * -20000), 10) >= 5;
	
	vec4 lineColor = vec4(1, 1, 1, 0.9);
	if (line) lineColor = vec4(1, 0, 1, 0.6);
	
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator * lineColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
