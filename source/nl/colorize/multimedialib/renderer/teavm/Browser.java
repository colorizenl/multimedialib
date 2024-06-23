//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.pixi.PixiBridge;
import nl.colorize.multimedialib.renderer.three.ThreeBridge;
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

    @JSBody(
        params = {"message"},
        script = "window.alert(message);"
    )
    public static native void alert(String message);

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

    @JSBody(script = "return (\"ontouchstart\" in window) || navigator.maxTouchPoints > 0")
    public static native boolean isTouchSupported();

    @JSBody(script = "window.prepareAnimationLoop();")
    public static native void prepareAnimationLoop();

    @JSBody(
        params = {"callback"},
        script = "window.registerErrorHandler(callback);"
    )
    public static native void registerErrorHandler(ErrorCallback callback);

    @JSBody(
        params = {"family", "url", "errorCallback"},
        script = "return window.preloadFontFace(family, url, errorCallback);"
    )
    public static native void preloadFontFace(String family, String url, ErrorCallback errorCallback);

    @JSBody(
        params = {"text"},
        script = "window.navigator.clipboard.writeText(text);"
    )
    public static native void writeClipboard(String text);

    @JSBody(
        params = {"name", "defaultValue"},
        script = "return window.getMeta(name, defaultValue);"
    )
    public static native String getMeta(String name, String defaultValue);

    @JSBody(
        params = {"label", "initial", "callback"},
        script = "window.requestTextInput(label, initial, callback);"
    )
    public static native void requestTextInput(String label, String initial, MessageCallback callback);

    @JSBody(
        params = {"appName"},
        script = "window.loadApplicationData(appName);"
    )
    public static native void loadApplicationData(String appName);

    @JSBody(
        params = {"appName", "name", "value"},
        script = "window.saveApplicationData(appName, name, value);"
    )
    public static native void saveApplicationData(String appName, String name, String value);

    // -------------------------------
    // JavaScript framework interfaces
    // -------------------------------

    @JSBody(script = "return window.pixiBridge;")
    public static native PixiBridge getPixiBridge();

    @JSBody(script = "return window.threeBridge;")
    public static native ThreeBridge getThreeBridge();

    @JSBody(script = "return window.peerjsBridge;")
    public static native PeerjsBridge getPeerJsBridge();
}
