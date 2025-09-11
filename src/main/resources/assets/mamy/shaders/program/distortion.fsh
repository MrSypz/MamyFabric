#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float DistortionStrength;
uniform vec2 ArrowScreenPos;  // Screen position of arrow (0-1 coordinates)
uniform float DistortionRadius; // Radius of effect

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Calculate distance from arrow position
    float distanceFromArrow = distance(texCoord, ArrowScreenPos);

    // Create circular mask around arrow position
    float mask = 1.0 - smoothstep(0.0, DistortionRadius, distanceFromArrow);

    vec2 distortionOffset = vec2(
    sin(Time * 8.0 + texCoord.y * 20.0) * 0.005,
    cos(Time * 6.0 + texCoord.x * 15.0) * 0.003
    ) * DistortionStrength * mask;

    vec3 color = texture(DiffuseSampler, texCoord + distortionOffset).rgb;
    fragColor = vec4(color, 1.0);
}