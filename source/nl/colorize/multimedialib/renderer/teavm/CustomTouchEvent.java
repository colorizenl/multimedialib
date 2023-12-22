//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

/**
 * Interface for custom events that are used to simulate touch events. This is
 * necessary because TeaVM does not provide bindings for "native" touch events
 * yet. Since these events are simulated, the API is a bit different from the
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Touch_events">
 * browser touch event API</a>. The custom events are created and dispatched
 * from the MultimediaLib JavaScript code.
 */
public interface CustomTouchEvent extends Event {

    @JSProperty
    public CustomTouchEventDetails getDetail();

    public interface CustomTouchEventDetails extends JSObject {

        @JSProperty
        public int getIdentifier();

        @JSProperty
        public int getPageX();

        @JSProperty
        public int getPageY();
    }
}
