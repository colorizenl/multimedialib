//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback function that is invoked from JavaScript when an AJAX request has
 * been sent and the response has been received.
 */
@JSFunctor
@FunctionalInterface
public interface AjaxCallback extends JSObject {

    public void onResponse(String response);
}
