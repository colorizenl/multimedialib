//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.pixi.PixiInterface;
import nl.colorize.multimedialib.renderer.three.ThreeInterface;
import org.teavm.jso.JSBody;

/**
 * Contains the API for calling JavaScript functions using TeaVM. This consists
 * of general browser APIs and bindings to the parts of MultimediaLib that are
 * implemented in JavaScript. This class therefore acts as the bridge between
 * the TeaVM renderer implementation in Java code, and the "native" browser
 * code implemented in JavaScript.
 */
public class Browser {

    @JSBody(
        params = {"message"},
        script = "console.log(message);"
    )
    public static native void log(String message);

    @JSBody(script = "return window.location.href;")
    public static native String getPageURL();

    @JSBody(script = "return window.location.search.toString();")
    public static native String getPageQueryString();

    @JSBody(script = "return navigator.userAgent;")
    public static native String getUserAgent();

    @JSBody(script = "return document.documentElement.clientWidth;")
    public static native float getPageWidth();

    @JSBody(script = "return document.documentElement.clientHeight;")
    public static native float getPageHeight();

    @JSBody(script = "return window.screen.width;")
    public static native int getScreenWidth();

    @JSBody(script = "return window.screen.height;")
    public static native int getScreenHeight();

    @JSBody(script = "return window.devicePixelRatio;")
    public static native float getDevicePixelRatio();

    @JSBody(script = "return window.ontouchstart !== undefined;")
    public static native boolean isTouchSupported();

    @JSBody(
        params = {"key", "value"},
        script = "window.localStorage.setItem(key, value);"
    )
    public static native void setLocalStorage(String key, String value);

    @JSBody(
        params = {"key"},
        script = "return window.localStorage.getItem(key);"
    )
    public static native String getLocalStorage(String key);

    @JSBody(
        params = {"label", "initialValue"},
        script = "return window.prompt(label, initialValue);"
    )
    public static native String prompt(String label, String initialValue);

    @JSBody(script = "window.prepareAnimationLoop();")
    public static native void prepareAnimationLoop();

    @JSBody(
        params = {"callback"},
        script = "window.registerErrorHandler(callback);"
    )
    public static native void registerErrorHandler(ErrorCallback callback);

    // ----------------------------------------
    // JavaScript framework interfaces
    // ----------------------------------------

    @JSBody(script = "return window.pixiInterface;")
    public static native PixiInterface getPixiInterface();

    @JSBody(script = "return window.threeInterface;")
    public static native ThreeInterface getThreeInterface();
}
