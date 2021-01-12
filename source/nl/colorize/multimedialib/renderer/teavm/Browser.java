//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSBody;

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

    @JSBody(script = "window.localStorage.clear();")
    public static native void clearLocalStorage();

    @JSBody(
        params = {"callback"},
        script = "startAnimationLoop(callback);"
    )
    public static native void startAnimationLoop(AnimationFrameCallback callback);

    @JSBody(script = "return canvas.width;")
    public static native float getCanvasWidth();

    @JSBody(script = "return canvas.height;")
    public static native float getCanvasHeight();

    @JSBody(script = "return getRequestedRenderer();")
    public static native String getRendererType();

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

    @JSBody(script = "return takeScreenshot();")
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

    @JSBody(
        params = {"meshId", "path", "callback"},
        script = "renderer.loadModel(meshId, path, callback);"
    )
    public static native void loadModel(String meshId, String path, ModelLoadCallback callback);

    @JSBody(
        params = {"path"},
        script = "renderer.getTexture(path);"
    )
    public static native void loadTexture(String path);



    //-------------------------------------------------------------------------
    // Network
    //-------------------------------------------------------------------------

    @JSBody(
        params = {"url", "headers", "callback"},
        script = "sendGetRequest(url, headers, callback);"
    )
    public static native void sendGetRequest(String url, String[] headers, AjaxCallback callback);

    @JSBody(
        params = {"url", "headers", "params", "callback"},
        script = "sendPostRequest(url, headers, params, callback);"
    )
    public static native void sendPostRequest(String url, String[] headers, String params,
                                              AjaxCallback callback);

    @JSBody(script = "return \"WebSocket\" in window;")
    public static native boolean isWebSocketSupported();

    @JSBody(
        params = {"uri", "callback"},
        script = "connectWebSocket(uri, callback);"
    )
    public static native void connectWebSocket(String uri, ConnectionCallback callback);

    @JSBody(
        params = {"message"},
        script = "sendWebSocket(message);"
    )
    public static native void sendWebSocket(String message);

    @JSBody(script = "closeWebSocket();")
    public static native void closeWebSocket();

    @JSBody(
        params = {"id", "receiveCallback"},
        script = "openPeerConnection(id, receiveCallback);"
    )
    public static native void openPeerConnection(String id, ConnectionCallback receiveCallback);

    @JSBody(
        params = {"message"},
        script = "sendPeerMessage(message);"
    )
    public static native void sendPeerMessage(String message);

    @JSBody(script = "closePeerConnection();")
    public static native void closePeerConnection();



    //-------------------------------------------------------------------------
    // 2D graphics
    //-------------------------------------------------------------------------

    @JSBody(
        params = {"x0", "y0", "x1", "y1", "color", "thickness"},
        script = "renderer.drawLine(x0, y0, x1, y1, color, thickness);"
    )
    public static native void drawLine(float x0, float y0, float x1, float y1, String color, float thickness);

    @JSBody(
        params = {"x", "y", "width", "height", "color", "alpha"},
        script = "renderer.drawRect(x, y, width, height, color, alpha);"
    )
    public static native void drawRect(float x, float y, float width, float height,
                                       String color, float alpha);

    @JSBody(
        params = {"x", "y", "radius", "color", "alpha"},
        script = "renderer.drawCircle(x, y, radius, color, alpha);"
    )
    public static native void drawCircle(float x, float y, float radius, String color, float alpha);

    @JSBody(
        params = {"points", "color", "alpha"},
        script = "renderer.drawPolygon(points, color, alpha);"
    )
    public static native void drawPolygon(float[] points, String color, float alpha);

    @JSBody(
        params = {"id", "x", "y", "width", "height", "alpha", "mask"},
        script = "renderer.drawImage(id, x, y, width, height, alpha, mask);"
    )
    public static native void drawImage(String id, float x, float y, float width, float height,
                                        float alpha, String mask);

    @JSBody(
        params = {"id", "regionX", "regionY", "regionWidth", "regionHeight", "x", "y",
            "width", "height", "rotation", "scaleX", "scaleY", "alpha", "mask"},
        script = "renderer.drawImageRegion(id, regionX, regionY, regionWidth, regionHeight, " +
            "                         x, y, width, height, rotation, scaleX, scaleY, alpha, mask);"
    )
    public static native void drawImageRegion(String id, float regionX, float regionY,
                                              float regionWidth, float regionHeight,
                                              float x, float y, float width, float height,
                                              float rotation, float scaleX, float scaleY,
                                              float alpha, String mask);

    @JSBody(
        params = {"text", "font", "size", "color", "bold", "x", "y", "align", "alpha"},
        script = "renderer.drawText(text, font, size, color, bold, x, y, align, alpha);"
    )
    public static native void drawText(String text, String font, int size, String color, boolean bold,
                                       float x, float y, String align, float alpha);

    @JSBody(
        params = {"originalId", "newId", "color"},
        script = "tintImage(originalId, newId, color);"
    )
    public static native void tintImage(String originalId, String newId, String color);



    //-------------------------------------------------------------------------
    // 3D graphics
    //-------------------------------------------------------------------------

    @JSBody(
        params = {"color"},
        script = "renderer.changeAmbientLight(color);"
    )
    public static native void changeAmbientLight(String color);

    @JSBody(
        params = {"color"},
        script = "renderer.changeLight(color);"
    )
    public static native void changeLight(String color);

    @JSBody(
        params = {"x", "y", "z", "targetX", "targetY", "targetZ"},
        script = "renderer.moveCamera(x, y, z, targetX, targetY, targetZ);"
    )
    public static native void moveCamera(float x, float y, float z,
                                         float targetX, float targetY, float targetZ);

    @JSBody(
        params = {"meshId", "sizeX", "sizeY", "sizeZ", "color", "texturePath"},
        script = "renderer.createBox(meshId, sizeX, sizeY, sizeZ, color, texturePath);"
    )
    public static native void createBox(String meshId, float sizeX, float sizeY, float sizeZ,
                                        String color, String texturePath);

    @JSBody(
        params = {"meshId", "diameter", "color", "texturePath"},
        script = "renderer.createSphere(meshId, diameter, color, texturePath);"
    )
    public static native void createSphere(String meshId, float diameter,
                                           String color, String texturePath);

    @JSBody(
        params = {"modelId", "meshId"},
        script = "renderer.addModel(modelId, meshId);"
    )
    public static native void addModel(String modelId, String meshId);

    @JSBody(
        params = {"modelId"},
        script = "renderer.removeModel(modelId);"
    )
    public static native void removeModel(String modelId);

    @JSBody(script = "renderer.clearModels();")
    public static native void clearModels();

    @JSBody(
        params = {"modelId", "x", "y", "z", "rotX", "rotY", "rotZ", "scaleX", "scaleY", "scaleZ"},
        script = "renderer.syncModel(modelId, x, y, z, rotX, rotY, rotZ, scaleX, scaleY, scaleZ);"
    )
    public static native void syncModel(String modelId, float x, float y, float z,
                                        float rotX, float rotY, float rotZ,
                                        float scaleX, float scaleY, float scaleZ);

    @JSBody(
        params = {"modelId", "meshId", "name", "loop"},
        script = "renderer.playAnimation(modelId, meshId, name, loop);"
    )
    public static native void playAnimation(String modelId, String meshId, String name, boolean loop);
}
