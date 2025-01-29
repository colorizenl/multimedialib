//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSObject;

/**
 * TeaVM interface for the {@code browser-bridge.js} JavaScript implementation.
 */
public interface BrowserBridge extends JSObject {

    public boolean isMobileDevice();

    public boolean isTouchSupported();

    public String getQueryParameter(String name, String defaultValue);

    public String getMeta(String name, String defaultValue);

    public void prepareAnimationLoop();

    public void registerErrorHandler(ErrorCallback callback);

    public void preloadFontFace(String family, String url, ErrorCallback errorCallback);

    public void writeClipboard(String text, ErrorCallback callback);

    public void requestTextInput(String label, String initial, MessageCallback callback);

    public void loadApplicationData();

    public void saveApplicationData(String name, String value);
}
