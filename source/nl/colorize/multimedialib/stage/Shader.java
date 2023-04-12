//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.renderer.MediaAsset;

/**
 * Parsed and compiled OpenGL shader, consisting of both a vertex shader and a
 * fragment shader.
 */
public interface Shader extends MediaAsset {

    /**
     * No-op shader implementation that can be used by renderers that do not
     * support shaders.
     */
    public static final Shader NO_OP = () -> false;

    /**
     * Returns true if the current renderer supports shaders. If this returns
     * false it means this shader is a no-op implementation and will not affect
     * graphics.
     */
    public boolean isSupported();
}
