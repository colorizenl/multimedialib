//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.renderer.teavm.PeerMessage;

/**
 * Access to a currently active peer-to-peer connection, which can be obtained
 * from {@link Network}. The protocol used for the connection depends on both
 * the renderer and the current platform. The same {@link PeerConnection}
 * instance can be connected to <em>multiple</em> peers.
 */
public interface PeerConnection {

    /**
     * Attempts to connect to the peer with the specified ID. This will lead
     * to a message of type {@link PeerMessage#TYPE_CONNECT} or type
     * {@link PeerMessage#TYPE_ERROR}, depending on the result.
     */
    public void connect(String peerId);

    /**
     * Sends the specified data message to all connected peers. The recipients
     * will receive a message of type {@link PeerMessage#TYPE_DATA}.
     */
    public void sendMessage(String message);

    /**
     * Returns all received messages from all connected peers.
     */
    public Buffer<PeerMessage> getReceivedMessages();
}
