//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM interface for the {@code peerjs-bridge.js} JavaScript implementation.
 */
public interface PeerjsBridge extends JSObject {

    @JSProperty
    public boolean isInitialized();

    public void open(MessageCallback messageCallback);

    public void connect(String peerId, MessageCallback messageCallback);

    public void sendMessage(String message);

    public void sendMessageToPeer(String peerId, String message);
}
