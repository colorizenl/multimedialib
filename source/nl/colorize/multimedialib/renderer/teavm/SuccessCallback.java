//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Semi-generic JavaScript function callback that can be used to return the
 * result of an asynchronous operation that can either succeed or fail.
 */
@JSFunctor
@FunctionalInterface
public interface SuccessCallback extends JSObject {

    public void onOperationCompleted(boolean success);
}
