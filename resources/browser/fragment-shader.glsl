//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

precision mediump float;

varying vec4 vColor;
varying vec2 vTextureCoordinates;
uniform sampler2D uTexture;

void main() {
    if (vTextureCoordinates.x >= 0.0 && vTextureCoordinates.y >= 0.0) {
        gl_FragColor = texture2D(uTexture, vTextureCoordinates);
        gl_FragColor.rgb *= gl_FragColor.a;
    } else {
        gl_FragColor = vColor;
    }
}
