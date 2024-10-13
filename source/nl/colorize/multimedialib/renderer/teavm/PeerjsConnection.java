//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.util.MessageQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the {@link PeerConnection} interface in Java, which is then
 * bridged to the PeerJS implementation in JavaScript via TeaVM.
 */
public class PeerjsConnection implements PeerConnection, MessageCallback {

    private PeerjsBridge bridge;
    private MessageQueue<String> connectionQueue;
    private MessageQueue<PeerMessage> receivedBuffer;
    private List<String> sendHistory;

    protected PeerjsConnection(PeerjsBridge bridge) {
        this.bridge = bridge;
        this.connectionQueue = new MessageQueue<>();
        this.receivedBuffer = new MessageQueue<>();
        this.sendHistory = new ArrayList<>();

        bridge.open(this);
    }

    @Override
    public void connect(String peerId) {
        connectionQueue.offer(peerId);
        processConnectionQueue();
    }

    private void processConnectionQueue() {
        if (bridge.isInitialized()) {
            for (String peerId : connectionQueue.flush()) {
                bridge.connect(peerId, this);
            }
        }
    }

    @Override
    public void sendMessage(String message) {
        bridge.sendMessage(message);
        sendHistory.add(message);
    }

    @Override
    public MessageQueue<PeerMessage> getReceivedMessages() {
        return receivedBuffer;
    }

    @Override
    public void onMessage(String type, String value) {
        receivedBuffer.offer(new PeerMessage(type, value));

        if (type.equals(PeerMessage.TYPE_INIT)) {
            processConnectionQueue();
        } else if (type.equals(PeerMessage.TYPE_CONNECT)) {
            sendHistory.forEach(message -> bridge.sendMessageToPeer(value, message));
        }
    }
}
