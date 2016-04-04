attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec2 a_TextureCoordinates;

varying vec4 v_Color;
varying vec2 v_TextureCoordinates;

void main() {
	v_Color = a_Color;
	v_TextureCoordinates = a_TextureCoordinates;
	gl_Position = a_Position;
}
