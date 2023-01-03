//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import nl.colorize.multimedialib.stage.Shader;

public class PixiShader implements Shader {

    private String vertexGLSL;
    private String fragmentGLSL;
    private PixiFilter filter;

    public PixiShader(String vertexGLSL, String fragmentGLSL) {
        this.vertexGLSL = vertexGLSL;
        this.fragmentGLSL = fragmentGLSL;
    }

    public void compile(PixiInterface pixi) {
        if (filter == null) {
            filter = pixi.createFilter(vertexGLSL, fragmentGLSL);
        }
    }

    public PixiFilter getFilter() {
        return filter;
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
