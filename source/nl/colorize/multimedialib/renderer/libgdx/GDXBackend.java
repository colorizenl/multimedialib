//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import nl.colorize.multimedialib.renderer.Canvas;

/**
 * Provided to {@link GDXRenderer} in order to support multiple back-ends for
 * the libGDX renderer.
 */
public interface GDXBackend {

    public void start(ApplicationListener app, Canvas canvas);

    public int getFramerate();
}
