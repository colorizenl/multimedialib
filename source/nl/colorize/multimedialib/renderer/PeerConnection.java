//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import java.util.List;

/**
 * Connection for sending text-based messages in a peer-to-peer network.
 * Messages are received asunchronously. Use {@link #flushReceivedMessages()}
 * to process received messages during the animation loop. This will return
 * the list of messages that was received since the last time the method was
 * called. A more traditional message listener/observer interface is not used
 * as it would interfere with the animation loop.
 */
public interface PeerConnection {

    public List<String> flushReceivedMessages();

    public void sendMessage(String message);
}
