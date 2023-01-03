//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.three;

import nl.colorize.multimedialib.stage.Shader;

public class ThreeShader implements Shader {

    private String vertexGLSL;
    private String fragmentGLSL;

    public ThreeShader(String vertexGLSL, String fragmentGLSL) {
        this.vertexGLSL = vertexGLSL;
        this.fragmentGLSL = fragmentGLSL;
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
