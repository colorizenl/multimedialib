//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * JavaScript function interface that is used to construct {@link PeerMessage}s
 * from messages received in callback functions.
 */
@JSFunctor
@FunctionalInterface
public interface MessageCallback extends JSObject {

    public void onMessage(String type, String value);
}
