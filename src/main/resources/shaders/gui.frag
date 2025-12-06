#version 330 core

in vec2 vTexCoord;
in vec4 vColor;

out vec4 fragColor;

uniform sampler2D uTexture;
uniform int uUseTexture;

void main() {
    if (uUseTexture == 1) {
        fragColor = texture(uTexture, vTexCoord) * vColor;
    } else {
        fragColor = vColor;
    }
}
