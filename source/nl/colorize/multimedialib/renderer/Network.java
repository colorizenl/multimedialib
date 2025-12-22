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

/**
 * Interface for the platform-specific mechanism for network access. HTTP
 * requests are sent asynchronously to avoid blocking the application. These
 * asynchronous operations return a {@link EventQueue} that can be used to
 * subscribe to the response.
 * <p>
 * MultimediaLib cannot use the standard HTTP client in {@code java.net.http},
 * as this is not yet available on all platforms supported by MultimediaLib.
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

    /**
     * Opens a peer-to-peer connection. The protocol used for the connection
     * depends on both the renderer and the current platform.
     *
     * @throws UnsupportedOperationException if peer-to-peer connections are
     *         not supported by the current platform and/or renderer.
     */
    public PeerConnection openPeerConnection();

    /**
     * Returns true if this renderer supports peer-to-peer connections on
     * the current platform. If this method returns false, trying to open
     * peer-to-peer connections using {@link #openPeerConnection()} will
     * result in an exception.
     */
    public boolean isPeerToPeerSupported();
}
