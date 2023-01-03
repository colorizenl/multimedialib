#ifdef GL_ES
    precision mediump float;
#endif

varying vec2 v_texCoords;
varying vec4 v_color;
uniform sampler2D u_texture;

void main() {
    vec4 c = texture2D(u_texture, v_texCoords);
    gl_FragColor.r = (c.r * 0.393) + (c.g * 0.769) + (c.b * 0.189);
    gl_FragColor.g = (c.r * 0.349) + (c.g * 0.686) + (c.b * 0.168);
    gl_FragColor.b = (c.r * 0.272) + (c.g * 0.534) + (c.b * 0.131);
    gl_FragColor.a = c.a;
}
