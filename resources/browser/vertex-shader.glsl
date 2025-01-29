//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

attribute vec2 aVertexPosition;
attribute vec2 aTextureCoordinates;
uniform vec4 uColor;
uniform vec2 uPositionVector;
uniform vec2 uRotationVector;
uniform vec2 uScaleVector;

varying vec4 vColor;
varying vec2 vTextureCoordinates;

void main() {
    vec2 scaledPosition = aVertexPosition * uScaleVector;
    vec2 rotatedPosition = vec2(
        scaledPosition.x * uRotationVector.y + scaledPosition.y * uRotationVector.x,
        scaledPosition.y * uRotationVector.y - scaledPosition.x * uRotationVector.x
    );
    gl_Position = vec4(rotatedPosition + uPositionVector, 0.0, 1.0);

    vColor = uColor;
    vTextureCoordinates = aTextureCoordinates;
}
