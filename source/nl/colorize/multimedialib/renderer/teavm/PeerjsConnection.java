//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.util.SubscribableCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Implements the {@link PeerConnection} interface in Java, which is then
 * bridged to the PeerJS implementation in JavaScript via TeaVM.
 */
public class PeerjsConnection implements PeerConnection, MessageCallback {

    private PeerjsBridge bridge;
    private SubscribableCollection<String> connectionQueue;
    private SubscribableCollection<PeerMessage> receivedBuffer;
    private List<String> sendHistory;

    protected PeerjsConnection(PeerjsBridge bridge) {
        this.bridge = bridge;
        this.connectionQueue = SubscribableCollection.wrap(new CopyOnWriteArrayList<>());
        this.receivedBuffer = SubscribableCollection.wrap(new CopyOnWriteArrayList<>());
        this.sendHistory = new ArrayList<>();

        bridge.open(this);
    }

    @Override
    public void connect(String peerId) {
        connectionQueue.add(peerId);
        processConnectionQueue();
    }

    private void processConnectionQueue() {
        if (bridge.isInitialized()) {
            connectionQueue.flush().forEach(peerId -> bridge.connect(peerId, this));
        }
    }

    @Override
    public void sendMessage(String message) {
        bridge.sendMessage(message);
        sendHistory.add(message);
    }

    @Override
    public Iterable<PeerMessage> flushReceivedMessages() {
        Stream<PeerMessage> messages = receivedBuffer.flush();
        return messages::iterator;
    }

    @Override
    public void onMessage(String type, String value) {
        receivedBuffer.add(new PeerMessage(type, value));

        if (type.equals(PeerMessage.TYPE_INIT)) {
            processConnectionQueue();
        } else if (type.equals(PeerMessage.TYPE_CONNECT)) {
            sendHistory.forEach(message -> bridge.sendMessageToPeer(value, message));
        }
    }
}
