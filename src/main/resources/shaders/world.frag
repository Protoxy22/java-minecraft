#version 330 core

in vec2 vTexCoord;
in vec4 vColor;
in float vFogDistance;

out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec4 uFogColor;
uniform float uFogStart;
uniform float uFogEnd;
uniform int uFogEnabled;

void main() {
    vec4 texColor = texture(uTexture, vTexCoord);
    vec4 baseColor = texColor * vColor;
    
    if (uFogEnabled == 1) {
        float fogFactor = clamp((uFogEnd - vFogDistance) / (uFogEnd - uFogStart), 0.0, 1.0);
        fragColor = mix(uFogColor, baseColor, fogFactor);
    } else {
        fragColor = baseColor;
    }
}
