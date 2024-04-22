//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

/**
 * Data structure for messages received from a peer-to-peer connection.
 * Messages are always in text-based form, even if the underlying protocol
 * is based on binary messages.
 */
public record PeerMessage(String type, String value) {

    /**
     * Message type for when this peer connection has completed initialization.
     * The message value is this peer's connection ID, which can be used by
     * other peers to connect.
     */
    public static final String TYPE_INIT = "init";

    /**
     * Message type for when a new peer has connected. The message value
     * is the peer's connection ID.
     */
    public static final String TYPE_CONNECT = "connect";

    /**
     * Message type for when a previously connected peer has been disconnected,
     * for whatever reason. The message value is the peer's connection ID.
     */
    public static final String TYPE_DISCONNECT = "disconnect";

    /**
     * Message type for when a data message has been received. The message
     * value is the plain text version of the data that has been received.
     */
    public static final String TYPE_DATA = "data";

    /**
     * Message type used for connection errors. The message value provides
     * details on the error.
     */
    public static final String TYPE_ERROR = "error";
}
