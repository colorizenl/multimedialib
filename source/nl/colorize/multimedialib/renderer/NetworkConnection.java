//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
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
    private MessageBuffer sendBuffer;
    private MessageBuffer receiveBuffer;
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
        this.sendBuffer = new MessageBuffer();
        this.receiveBuffer = new MessageBuffer();
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

    /**
     * Data structure that acts as a communication channel between a message producer
     * and consumer that operate at different intervals. The producer adds messages
     * to the buffer as they come in. The consumer then periodically flushes the
     * message buffer to process the received messages.
     * <p>
     * This class is thread safe, the producer and consumer can access the message
     * buffer when operating from different threads.
     */
    private static class MessageBuffer {

        private List<String> messages;

        public MessageBuffer() {
            this.messages = new ArrayList<>();
        }

        public synchronized void add(String message) {
            messages.add(message);
        }

        /**
         * Clears the message buffer and returns all messages that have been added
         * since the last time this method was called.
         */
        public synchronized List<String> flush() {
            if (messages.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> contents = ImmutableList.copyOf(messages);
            messages.clear();
            return contents;
        }

        /**
         * Calls {@link #flush()} and operates the provided callback on every
         * received message.
         */
        public void flush(Consumer<String> callback) {
            flush().forEach(callback);
        }

        public synchronized void reset() {
            messages.clear();
        }
    }
}
