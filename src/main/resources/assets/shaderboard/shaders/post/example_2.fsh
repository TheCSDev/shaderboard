// ==================================================
#version 330
#extension GL_ARB_separate_shader_objects : require
// --------------------------------------------------
#include <minecraft:globals.glsl>
// ==================================================
uniform sampler2D InSampler;
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;
// --------------------------------------------------
layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};
// --------------------------------------------------
//from "shaderboard:bindings":
layout(std140) uniform u_MyHealth {
	float health;
	float maxHealth;
	float absorption;
};
// ==================================================
const float BAR_HEIGHT       = 0.02;
const float BAR_TOP          = 0.95;
const float BAR_RATIO        = 182.0 / 5.0;

const vec3  HEALTH_COLOR     = vec3(1.00, 0.00, 0.00);
const vec3  ABSORPTION_COLOR = vec3(1.00, 1.00, 0.00);
const vec3  TRACK_COLOR      = vec3(0.00, 0.00, 0.00);
// ==================================================
bool isInsideBar(vec2 uv, float left, float right, float bottom, float top) {
	return uv.x >= left && uv.x <= right && uv.y >= bottom && uv.y <= top;
}
// ==================================================
void main()
{
	//the starting 'output = input' color
	vec3 rgb = texture(InSampler, texCoord).rgb;

	//turn the raw values into fractions
	float health01     = clamp(health     / maxHealth, 0.0, 1.0);
	float absorption01 = clamp(absorption / maxHealth, 0.0, 1.0);
	float missing01    = 1.0 - health01; //0.0 = full health, 1.0 = no health

	//red vignette that intensifies as health drops
	float edge = smoothstep(0.30, 0.90, length(texCoord - 0.5) * 1.5);
	rgb = mix(rgb, vec3(0.45, 0.0, 0.0), edge * missing01 * missing01 * 0.85);

	//dim the scene when close to death
	rgb *= mix(1.0, 0.6, smoothstep(0.55, 1.0, missing01));

	//the health bar
	float barWidth  = BAR_HEIGHT * (OutSize.y / OutSize.x) * BAR_RATIO;
	float barLeft   = 0.5 - barWidth * 0.5;
	float barRight  = 0.5 + barWidth * 0.5;
	float barBottom = BAR_TOP - BAR_HEIGHT;

	if(health01 > 0 && isInsideBar(texCoord, barLeft, barRight, barBottom, BAR_TOP)) {
		//obtain how far along the progress bar this pixel is
		float progress = (texCoord.x - barLeft) / barWidth;
		//draw the progress and hp bar
		if(progress <= health01)     rgb = HEALTH_COLOR;
		else                         rgb = TRACK_COLOR;
		//draw the absorption bar on top
		if(progress <= absorption01) rgb = ABSORPTION_COLOR;
	}

	//output
	fragColor = vec4(rgb, 1.0);
}
// ==================================================
