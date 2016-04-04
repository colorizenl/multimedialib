precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec4 v_Color;
varying vec2 v_TextureCoordinates;

void main() {
	if (v_TextureCoordinates[0] != -1 && v_TextureCoordinates[1] != -1) {
		gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
	} else {
		gl_FragColor = v_Color;
	}
}
