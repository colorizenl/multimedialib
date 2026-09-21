//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * JavaScript function interface that is used to receive messages in callback
 * functions to process asynchronous events.
 */
@JSFunctor
@FunctionalInterface
public interface MessageCallback extends JSObject {

    public void onMessage(String id, String message);
}
