//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSBody;

/**
 * Contains the API for calling JavaScript functions using TeaVM. This consists
 * of general browser APIs, as well as drawing operations for the HTML5 canvas
 * that is displaying the application.
 */
public class Browser {

    @JSBody(
        params = {"message"},
        script = "console.log(message);"
    )
    public static native void log(String message);

    @JSBody(script = "return window.location.href;")
    public static native String getPageURL();

    @JSBody(script = "return navigator.userAgent;")
    public static native String getUserAgent();

    @JSBody(
        params = {"url"},
        script = "sendGetRequest(url);"
    )
    public static native void sendGetRequest(String url);

    @JSBody(
        params = {"url", "params"},
        script = "sendPostRequest(url, params);"
    )
    public static native void sendPostRequest(String url, String params);

    @JSBody(script = "return document.documentElement.clientWidth;")
    public static native float getPageWidth();

    @JSBody(script = "return document.documentElement.clientHeight;")
    public static native float getPageHeight();

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

    @JSBody(script = "window.localStorage.clear();")
    public static native void clearLocalStorage();

    @JSBody(
        params = {"callback"},
        script = "onFrame(callback);"
    )
    public static native void renderFrame(AnimationFrameCallback callback);

    @JSBody(script = "return canvas.width;")
    public static native float getCanvasWidth();

    @JSBody(script = "return canvas.height;")
    public static native float getCanvasHeight();

    @JSBody(
        params = {"x", "y", "width", "height", "color", "alpha"},
        script = "drawRect(x, y, width, height, color, alpha);"
    )
    public static native void drawRect(float x, float y, float width, float height,
                                       String color, float alpha);

    @JSBody(
        params = {"points", "color", "alpha"},
        script = "drawPolygon(points, color, alpha);"
    )
    public static native void drawPolygon(float[] points, String color, float alpha);

    @JSBody(
        params = {"id", "x", "y", "width", "height", "alpha", "mask"},
        script = "drawImage(id, x, y, width, height, alpha, mask);"
    )
    public static native void drawImage(String id, float x, float y, float width, float height,
                                        float alpha, String mask);

    @JSBody(
        params = {"id", "regionX", "regionY", "regionWidth", "regionHeight", "x", "y",
                  "width", "height", "alpha", "mask"},
        script = "drawImageRegion(id, regionX, regionY, regionWidth, regionHeight, " +
                 "                x, y, width, height, alpha, mask);"
    )
    public static native void drawImageRegion(String id, float regionX, float regionY,
                                              float regionWidth, float regionHeight,
                                              float x, float y, float width, float height,
                                              float alpha, String mask);

    @JSBody(
        params = {"text", "font", "size", "color", "x", "y", "align"},
        script = "drawText(text, font, size, color, x, y, align);"
    )
    public static native void drawText(String text, String font, int size, String color,
                                       float x, float y, String align);

    @JSBody(script = "return pointerX;")
    public static native float getPointerX();

    @JSBody(script = "return pointerY;")
    public static native float getPointerY();

    @JSBody(script = "return pointerState;")
    public static native float getPointerState();

    @JSBody(
        params = {"keyCode"},
        script = "return keyStates[keyCode];"
    )
    public static native float getKeyState(int keyCode);

    @JSBody(
        params = {"id", "path"},
        script = "loadImage(id, path);"
    )
    public static native void loadImage(String id, String path);

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
        params = {"id", "path"},
        script = "audio[id] = new Audio(path);"
    )
    public static native void loadAudio(String id, String path);

    @JSBody(
        params = {"id"},
        script = "audio[id].play();"
    )
    public static native void playAudio(String id);

    @JSBody(
        params = {"id"},
        script = "audio[id].pause();"
    )
    public static native void stopAudio(String id);

    @JSBody(
        params = {"id", "path", "fontFamily"},
        script = "loadFont(id, path, fontFamily);"
    )
    public static native void loadFont(String id, String path, String fontFamily);

    @JSBody(
        params = {"id"},
        script = "return document.getElementById(id).innerHTML;"
    )
    public static native String loadTextResourceFile(String id);
}
