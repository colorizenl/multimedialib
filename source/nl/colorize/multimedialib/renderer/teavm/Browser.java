//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSBody;

/**
 * Entry point to access the various "bridge" interfaces that can be used to
 * call JavaScript functions using TeaVM. This consists of both general browser
 * APIs and bindings to the parts of MultimediaLib that are implemented in
 * JavaScript.
 */
public class Browser {

    @JSBody(script = "return window.browserBridge;")
    public static native BrowserBridge getBrowserBridge();

    @JSBody(script = "return window.pixiBridge;")
    public static native PixiBridge getPixiBridge();

    @JSBody(script = "return window.threeBridge;")
    public static native ThreeBridge getThreeBridge();

    @JSBody(script = "return window.peerjsBridge;")
    public static native PeerjsBridge getPeerJsBridge();

    /**
     * Returns true if the current platform can be considered a mobile device.
     * Note this check does not depend on whether the device supports touch
     * controls, since some desktop environments support both mouse and touch
     * control.
     * <p>
     * This method can be called from application launcher code.
     */
    @JSBody(script = "return window.browserBridge.isMobileDevice();")
    public static native boolean isMobileDevice();

    /**
     * Returns the query parameter with the specified name, or the default
     * value if no such query parameter exists.
     * <p>
     * This method can be called from application launcher code. This enables
     * the use of query parameters to influence the launcher behavior.
     */
    @JSBody(
        params = {"name", "defaultValue"},
        script = "return window.browserBridge.getQueryParameter(name, defaultValue);"
    )
    public static native String getQueryParameter(String name, String defaultValue);
}
