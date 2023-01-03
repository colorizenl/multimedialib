//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import nl.colorize.multimedialib.stage.Shader;

public class GDXShader implements Shader {

    private ShaderProgram shaderProgram;

    protected GDXShader(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    protected ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
