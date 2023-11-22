//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.util.Subscribable;

/**
 * Access to a currently active peer-to-peer connection, which can be obtained
 * from {@link Network}. The protocol used for the connection depends on both
 * the renderer and the current platform. This also means that it is generally
 * not possible to connect to peers on other platforms.
 * <p>
 * Since this is a peer-to-peer connection, it is possible to connect to
 * <em>multiple</em> peers. In that situation, messages will be sent to
 * <em>all</em> peers, and received messages will similarly include all peers.
 */
public interface PeerConnection {

    public String getId();

    /**
     * Attempts to connect to the peer with the specified ID. If the connection
     * is successful, messages will be sent to and received from the peer until
     * it disconnects.
     */
    public Subscribable<String> connect(String peerId);

    public void sendMessage(String message);

    public Buffer<String> getReceivedMessages();

    public void close();
}
