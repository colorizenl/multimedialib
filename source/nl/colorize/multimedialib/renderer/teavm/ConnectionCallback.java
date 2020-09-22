//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback function that is invoked from JavaScript for network connections,
 * both for web socket connections and for WebRTC connections.
 */
@JSFunctor
@FunctionalInterface
public interface ConnectionCallback extends JSObject {

    public void onMessage(String message);
}
