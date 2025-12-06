#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;

out vec2 vTexCoord;
out vec4 vColor;
out float vFogDistance;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    vec4 viewPos = uView * uModel * vec4(aPosition, 1.0);
    gl_Position = uProjection * viewPos;
    
    vTexCoord = aTexCoord;
    vColor = aColor;
    
    // Calculate fog distance from camera
    vFogDistance = length(viewPos.xyz);
}
