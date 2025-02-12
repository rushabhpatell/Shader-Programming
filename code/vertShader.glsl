#version 430

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vColor;
out vec3 color;
uniform float offsetX;
uniform float offsetY;
uniform float size;
uniform int rotationState;

void main()
{
    // Apply size scaling and offsets
    vec4 pos = vPosition * vec4(size, size, 1.0, 1.0);

    // Apply rotation based on rotationState
    if (rotationState == 1) { // Down
        pos.xy = vec2(pos.x, -pos.y);
    } else if (rotationState == 2) { // Left
        pos.xy = vec2(-pos.y, pos.x);
    } else if (rotationState == 3) { // Right
        pos.xy = vec2(pos.y, -pos.x);
    }

    // Apply offsets for movement
    gl_Position = vec4(pos.x + offsetX, pos.y + offsetY, pos.z, 1.0);
    color = vColor; // Pass color to fragment shader
}
