//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Subject;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLResponse;

/**
 * Interface for the platform-specific mechanism for network access. HTTP
 * requests are sent asynchronously to avoid blocking the application. These
 * asynchronous operations will return a {@link Subject} that
 * applications can use to await the result. Callback methods will be invoked
 * from a thread that is compatible with the current renderer.
 */
public interface Network {

    /**
     * Sends a HTTP GET request to the specified URL. The request will be
     * performed asyncrhonously, the returned {@link Subject} can be
     * used to subscribe to the response.
     */
    public Subject<URLResponse> get(String url, Headers headers);

    /**
     * Sends a HTTP POST request to the specified URL. The request will be
     * performed asyncrhonously, the returned {@link Subject} can be
     * used to subscribe to the response.
     */
    public Subject<URLResponse> post(String url, Headers headers, PostData body);

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
