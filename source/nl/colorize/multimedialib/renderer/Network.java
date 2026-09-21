//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.EventQueue;
import nl.colorize.util.http.PostData;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for the platform-specific mechanism for network access. HTTP
 * requests are sent asynchronously to avoid blocking the application. These
 * asynchronous operations return a {@link EventQueue} that can be used to
 * process the response during frame updates.
 * <p>
 * This class does not use the standard HTTP client in {@code java.net.http},
 * as this API is not yet available on all platforms supported by
 * MultimediaLib.
 */
public interface Network {

    public EventQueue<Response> send(
        String method,
        String url,
        Map<String, String> headers,
        @Nullable String body
    );

    default EventQueue<Response> get(String url, Map<String, String> headers) {
        return send("GET", url, headers, null);
    }

    default EventQueue<Response> get(String url) {
        return send("GET", url, Collections.emptyMap(), null);
    }

    default EventQueue<Response> post(String url, Map<String, String> headers, String body) {
        return send("POST", url, headers, body);
    }

    default EventQueue<Response> post(String url, Map<String, String> headers, PostData body) {
        return post(url, headers, body.encode());
    }

    default EventQueue<Response> post(String url, PostData body) {
        return post(url, Collections.emptyMap(), body.encode());
    }

    /**
     * Returns true if this renderer supports peer-to-peer connections on
     * the current platform.
     */
    public boolean isPeerToPeerSupported();

    /**
     * Opens a peer-to-peer connection that will listen for other peers to
     * connect. Returns an {@link EventQueue} that can be used to process
     * all received messages during frame updates. This includes the
     * {@link PeerMessage#OPEN} initialization message that can be used to
     * determine this peer connection's ID, which can then be shared with
     * other peers so that they can join.
     *
     * @throws UnsupportedOperationException if peer-to-peer connections are
     *         not supported by the current platform or renderer. This can
     *         be checked using {@link #isPeerToPeerSupported()}.
     * @throws IllegalStateException if a peer connection is already open.
     */
    public EventQueue<PeerMessage> openPeerConnection();

    /**
     * Opens a peer-to-peer connection by connecting to the peer with the
     * specified ID. Returns an {@link EventQueue} that can be used to
     * process all received messages during frame updates. Failure to join
     * will result in an error message being added to the event queue.
     *
     * @throws UnsupportedOperationException if peer-to-peer connections are
     *         not supported by the current platform or renderer. This can
     *         be checked using {@link #isPeerToPeerSupported()}.
     * @throws IllegalStateException if a peer connection is already open.
     */
    public EventQueue<PeerMessage> joinPeerConnection(String peerId);

    /**
     * Sends a message to all currently active peer-to-peer connections.
     * Failure to send will result in an error message being added to the
     * event queue.
     *
     * @throws UnsupportedOperationException if peer-to-peer connections are
     *         not supported by the current platform or renderer. This can
     *         be checked using {@link #isPeerToPeerSupported()}.
     * @throws IllegalStateException if the peer-to-peer subsystem is not
     *         active, i.e. no peer-to-peer connections have been either
     *         opened or joined.
     */
    public void sendPeerConnection(String message);

    /**
     * Returns the IDs of all peers for which a peer-to-peer connection is
     * currently active. The result does not include the ID of this peer
     * connection.
     *
     * @throws UnsupportedOperationException if peer-to-peer connections are
     *         not supported by the current platform or renderer. This can
     *         be checked using {@link #isPeerToPeerSupported()}.
     * @throws IllegalStateException if the peer-to-peer subsystem is not
     *         active, i.e. no peer-to-peer connections have been either
     *         opened or joined.
     */
    public Set<String> getPeerConnectionIds();

    /**
     * Simple representation of an HTTP response, consisting only of the HTTP
     * status, the response headers, and the response body. This class exists
     * because the standard HTTP client in {@code java.net.http} is not yet
     * available on all platforms supported by MultimediaLib.
     * <p>
     * Headers are considered to be case-insensitive, so "Content-Type" and
     * "content-type" are equivalent. For implementing classes, This typically
     * means the headers are represented by a {@code TreeMap} that uses
     * {@link String#CASE_INSENSITIVE_ORDER} as a comparator.
     */
    public record Response(int status, Map<String, String> headers, String body) {

        public Optional<String> getHeader(String name) {
            return Optional.ofNullable(headers.get(name));
        }

        /**
         * @deprecated Use {@link #body()} instead. This method exists only
         *            to retain backward compatibility with older versions
         *            of MultimediaLib.
         */
        @Deprecated
        public String getBody() {
            return body();
        }
    }

    /**
     * One of the messages received from a peer-to-peer connection.
     * <p>
     * The special values {@link #CONNECT}, {@link #DISCONNECT}, and
     * {@link #ERROR} indicate the peer has (respectively) connected,
     * disconnected, or encountered an error.
     * <p>
     * The special value {@link #OPEN} is used when the peer connection
     * is initialized and can be used to determine the connection ID,
     * which can then be shared so that other peers can join.
     */
    public record PeerMessage(String peerId, String body) {

        public static final String OPEN = "$$open";
        public static final String CONNECT = "$$connect";
        public static final String DISCONNECT = "$$disconnect";
        public static final String ERROR = "$$error";

        public boolean isSystemMessage() {
            return body.startsWith("$$");
        }
    }
}
