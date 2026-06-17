#version 150

#moj_import <matrix.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform int EndPortalLayers;

in vec4 texProj0;

const vec3[] COLORS = vec3[](
        vec3(0.010, 0.012, 0.015),
        vec3(0.008, 0.014, 0.018),
        vec3(0.012, 0.016, 0.020),
        vec3(0.018, 0.020, 0.024),
        vec3(0.014, 0.022, 0.018),
        vec3(0.020, 0.018, 0.026),
        vec3(0.022, 0.030, 0.035),
        vec3(0.018, 0.040, 0.022),
        vec3(0.030, 0.025, 0.045),
        vec3(0.028, 0.022, 0.040),
        vec3(0.035, 0.030, 0.038),
        vec3(0.015, 0.055, 0.050),
        vec3(0.060, 0.035, 0.070),
        vec3(0.020, 0.080, 0.085),
        vec3(0.070, 0.090, 0.060),
        vec3(0.080, 0.060, 0.120)
);

const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 end_portal_layer(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );

    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));

    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);

    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

out vec4 fragColor;

void main() {
    vec3 color = textureProj(Sampler0, texProj0).rgb * COLORS[0];
    for (int i = 0; i < EndPortalLayers; i++) {
        color += textureProj(Sampler1, texProj0 * end_portal_layer(float(i + 1))).rgb * COLORS[i];
    }
    fragColor = vec4(color, 1.0);
}
