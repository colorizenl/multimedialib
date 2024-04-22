//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Bridge to the parts of the PeerJS interface that are implemented in
 * JavaScript. This interface is used by TeaVM.
 */
public interface PeerjsBridge extends JSObject {

    @JSProperty
    public boolean isInitialized();

    public void open(MessageCallback messageCallback);

    public void connect(String peerId, MessageCallback messageCallback);

    public void sendMessage(String message);

    public void sendMessageToPeer(String peerId, String message);
}
