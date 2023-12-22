//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Subscribable;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLResponse;

/**
 * Interface for the platform-specific mechanism for network access. HTTP
 * requests are sent asynchronously to avoid blocking the application.
 */
public interface Network {

    public Subscribable<URLResponse> get(String url, Headers headers);

    public Subscribable<URLResponse> post(String url, Headers headers, PostData body);

    /**
     * Opens a peer-to-peer connection. The protocol used for the connection
     * depends on both the renderer and the current platform. This also means
     * that it is generally not possible to connect to peers on other
     * platforms.
     *
     * @throws UnsupportedOperationException if peer-to-peer connections are
     *         not supported by the current platform and/or renderer.
     */
    public Subscribable<PeerConnection> openPeerConnection();

    /**
     * Returns true if this renderer supports peer-to-peer connectios on the
     * current platform. See {@link #openPeerConnection()}.
     */
    public boolean isPeerToPeerSupported();
}
