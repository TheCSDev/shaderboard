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
layout(std140) uniform u_TopScore   { float topScore; };   //from "shaderboard:bindings":
layout(std140) uniform u_MyScore	{ float myScore; };    //from "shaderboard:bindings":
layout(std140) uniform u_SteveScore { float steveScore; }; //from "shaderboard:bindings":
// ==================================================
/**
 * Draws an int32 as a horizontal binary "barcode" stripe.
 * @param value The int32 value to draw.
 * @param uvY   The normalized lower vertical boundary (V coordinate) of the stripe.
 * @param uvH   The normalized vertical thickness (height) of the stripe.
 */
void drawBinaryValue(int value, float uvY, float uvH)
{
	//if the texture coordinates are not within bounds of the horizontal
	//stripe this function draws, do nothing aka return
	if(!(texCoord.y >= uvY && texCoord.y < uvY + uvH)) return;

	//otherwise draw the current pixel of the horizontal stripe
	int bitIndex = 31 - clamp(int(texCoord.x * 32.0), 0, 31);
	int bitValue = (value >> bitIndex) & 1;
	fragColor    = vec4(vec3(bitValue), 1.0);
}
// --------------------------------------------------
void main()
{
	//'output = input' is the base output color
	fragColor = texture(InSampler, texCoord);

	//draw the 3 scoreboard values on screen
	drawBinaryValue(int(topScore),   0.975, 0.025);
	drawBinaryValue(int(myScore),    0.950, 0.025);
	drawBinaryValue(int(steveScore), 0.925, 0.025);
}
// ==================================================
