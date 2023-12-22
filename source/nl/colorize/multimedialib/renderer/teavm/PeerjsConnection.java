//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Subscribable;

import java.util.logging.Logger;

/**
 * Implements the {@link PeerConnection} interface in Java, which is then
 * bridged to the PeerJS implementation in JavaScript via TeaVM.
 */
public class PeerjsConnection implements PeerConnection {

    private PeerjsBridge bridge;

    private static final Logger LOGGER = LogHelper.getLogger(PeerjsConnection.class);

    protected PeerjsConnection(PeerjsBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public String getId() {
        return bridge.getId();
    }

    @Override
    public Subscribable<String> connect(String peerId) {
        Subscribable<String> subscribable = new Subscribable<>();

        bridge.connect(peerId, success -> {
            if (success) {
                subscribable.next(peerId);
            } else {
                subscribable.nextError(new RuntimeException("Failed to connect to peer"));
            }
        });

        return subscribable;
    }

    @Override
    public void sendMessage(String message) {
        bridge.sendMessage(message);
    }

    @Override
    public Buffer<String> getReceivedMessages() {
        Buffer<String> received = new Buffer<>();
        received.push(bridge.flushReceivedMessages());
        return received;
    }

    @Override
    public void close() {
        bridge.close();
    }
}
