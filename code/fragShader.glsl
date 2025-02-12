#version 430

in vec3 color;
out vec4 fragColor;
uniform int colorMode;

void main()
{
    if (colorMode == 0) {
        fragColor = vec4(1.0, 1.0, 0.0, 1.0); // Yellow
    } else if (colorMode == 1) {
        fragColor = vec4(0.5, 0.0, 0.5, 1.0); // Purple
    } else {
        fragColor = vec4(color, 1.0); // Gradient (use vertex colors)
    }
}
