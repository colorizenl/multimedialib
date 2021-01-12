//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback interface that is used when loading models, which is done
 * asyncronously.
 */
@JSFunctor
@FunctionalInterface
public interface ModelLoadCallback extends JSObject {

    public void onModelLoadComplete(String[] animNames, float[] animDurations);
}
