//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback function that is invoked from JavaScript during every frame update
 * performed by the browser.
 * <p>
 * The animation loop is implemented using the {@code requestAnimationFrame}
 * browser API. The same animation loop is used for both updates and rendering,
 * so rather than having two separate animation loops the difference is
 * indicated by different arguments being provided to the callback.
 */
@JSFunctor
@FunctionalInterface
public interface AnimationFrameCallback extends JSObject {

    public void onFrame(float deltaTime, boolean render);
}
