//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.teavm.pixi.PixiInterface;
import org.teavm.jso.JSBody;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLImageElement;

/**
 * Contains the API for calling JavaScript functions using TeaVM. This consists
 * of general browser APIs, as well as drawing operations for the HTML5 canvas
 * that is displaying the application.
 * <p>
 * <strong>Note for testing:</strong> All methods in this class are defined as
 * {@code static native} due to requirements from TeaVM. Applications should
 * therefore mock these methods when using the browser API in Java unit tests.
 * <p>
 * This class acts as the bridge between the TeaVM renderer implementation in
 * Java code, and the "native" browser code implemented in JavaScript.
 * Applications will therefore not access any of these methods directly, as
 * interaction with JavaScript is done through the more Java-like API provided
 * by the renderer.
 */
public class Browser {

    //-------------------------------------------------------------------------
    // General
    //-------------------------------------------------------------------------

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

    @JSBody(script = "return Object.keys(window.localStorage);")
    public static native String[] getLocalStorageKeys();

    @JSBody(script = "window.localStorage.clear();")
    public static native void clearLocalStorage();

    @JSBody(script = "return canvas;")
    public static native HTMLCanvasElement getCanvas();

    @JSBody(script = "return canvas.width;")
    public static native float getCanvasWidth();

    @JSBody(script = "return canvas.height;")
    public static native float getCanvasHeight();

    @JSBody(
        params = {"id"},
        script = "return images[id];"
    )
    public static native HTMLImageElement getImage(String id);

    @JSBody(
        params = {"id"},
        script = "return images[id].width;"
    )
    public static native float getImageWidth(String id);

    @JSBody(
        params = {"id"},
        script = "return images[id].height;"
    )
    public static native float getImageHeight(String id);

    @JSBody(
        params = {"imageId", "mask"},
        script = "return prepareImage(imageId, mask);"
    )
    public static native CanvasImageSource prepareImage(String imageId, String mask);

    @JSBody(
        params = {"originalId", "newId", "color"},
        script = "tintImage(originalId, newId, color);"
    )
    public static native void tintImage(String originalId, String newId, String color);

    @JSBody(
        params = {"id", "x", "y"},
        script = "return getImageData(id, x, y);"
    )
    public static native float[] getImageData(String id, int x, int y);

    @JSBody(
        params = {"id", "volume", "loop"},
        script = "playAudio(id, volume, loop);"
    )
    public static native void playAudio(String id, float volume, boolean loop);

    @JSBody(
        params = {"id", "reset"},
        script = "stopAudio(id, reset);"
    )
    public static native void stopAudio(String id, boolean reset);

    @JSBody(script = "return canvas.toDataURL();")
    public static native String takeScreenshot();

    //-------------------------------------------------------------------------
    // Input devices
    //-------------------------------------------------------------------------

    @JSBody(script = "return flushPointerEventBuffer();")
    public static native String[] flushPointerEventBuffer();

    @JSBody(
        params = {"keyCode"},
        script = "return keyStates[keyCode];"
    )
    public static native float getKeyState(int keyCode);

    @JSBody(
        params = {"label", "initialValue"},
        script = "return window.prompt(label, initialValue);"
    )
    public static native String prompt(String label, String initialValue);

    //-------------------------------------------------------------------------
    // Media loader
    //-------------------------------------------------------------------------

    @JSBody(
        params = {"id", "path"},
        script = "loadImage(id, path);"
    )
    public static native void loadImage(String id, String path);

    @JSBody(
        params = {"id", "path"},
        script = "loadAudio(id, path);"
    )
    public static native void loadAudio(String id, String path);

    @JSBody(
        params = {"id", "path", "fontFamily"},
        script = "loadFont(id, path, fontFamily);"
    )
    public static native void loadFont(String id, String path, String fontFamily);

    @JSBody(
        params = {"id"},
        script = "return loadTextFile(id);"
    )
    public static native String loadTextResourceFile(String id);

    //-------------------------------------------------------------------------
    // Pixi.js
    //-------------------------------------------------------------------------

    @JSBody(script = "return new PixiInterface();")
    public static native PixiInterface initPixiInterface();
}
