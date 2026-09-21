//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSObject;

/**
 * TeaVM interface for the {@code peerjs-bridge.js} JavaScript implementation.
 */
public interface PeerjsBridge extends JSObject {

    public void registerCallback(MessageCallback callback);

    public void open();

    public void join(String peerId);

    public void send(String message);

    public String[] getPeerConnectionIds();

    public boolean isConnectionInitialized();
}
