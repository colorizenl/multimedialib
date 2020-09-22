//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.MessageBuffer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Interaction between the application and a network connection for sending and
 * receiving text-based messages. Received messages are added to a buffer, which
 * can then be flushed from the application's animation loop to ensure all
 * incoming messages are processed within said animation loop.
 * <p>
 * This class provides the same API regardless of the protocol used by the
 * network connection. The protocols that are supported depend on the renderer
 * and on the current platform.
 * <p>
 * This class is thread safe, sending and receiving messages can be done from
 * multiple threads.
 */
public final class NetworkConnection {

    private AtomicBoolean connected;
    private String id;
    private MessageBuffer<String> sendBuffer;
    private MessageBuffer<String> receiveBuffer;
    private Consumer<String> sender;

    /**
     * Creates an instance that will use the specified callback for sending
     * messages. This callback is then reponsible for updating this class'
     * status using {@link #connect()} and {@link #disconnect()}, and to
     * add received messages using {@link #queueReceivedMessage(String)}.
     */
    public NetworkConnection(Consumer<String> sender) {
        this.connected = new AtomicBoolean(false);
        this.id = null;
        this.sendBuffer = new MessageBuffer<>();
        this.receiveBuffer = new MessageBuffer<>();
        this.sender = sender;
    }

    public void connect() {
        connected.set(true);

        // Send all messages that were added to the send queue
        // but could not be sent yet.
        for (String message : sendBuffer.flush()) {
            sender.accept(message);
        }
    }

    public void disconnect() {
        connected.set(false);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public synchronized void receiveID(String id) {
        this.id = id;
    }

    /**
     * Returns a unique ID that can be used to identify this connection. When
     * initializing new connections, this method will return {@code null} until
     * the connection has been assigned an ID.
     */
    public synchronized String getID() {
        return id;
    }

    /**
     * Adds a message to the send queue. The message will either be sent
     * immediately, or as soon as the connection is available.
     */
    public void send(String message) {
        if (isConnected()) {
            sender.accept(message);
        } else {
            sendBuffer.add(message);
        }
    }

    /**
     * Adds a receive message to the queue. The received message can be processed
     * later by calling {@link #receive()} from the application's animation loop.
     */
    public void queueReceivedMessage(String message) {
        receiveBuffer.add(message);
    }

    /**
     * Returns all messages that were received since the last time this method
     * was called.
     */
    public List<String> receive() {
        return receiveBuffer.flush();
    }
}
