//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Promise;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLResponse;

/**
 * Interface for the platform-specific mechanism for network access.
 * To prevent blocking the application, HTTP requests are sent asynchronous
 * and a callback function is invoked once the response has been received.
 */
public interface Network {

    public Promise<URLResponse> get(String url, Headers headers);

    public Promise<URLResponse> post(String url, Headers headers, PostData body);

    /**
     * Opens a peer-to-peer connection. This requires an ID to identify the
     * peer. This ID is implementation-specific, IDs obtained on one platform
     * will not work on another platform/
     *
     * @throws UnsupportedOperationException if the renderer and/or the current
     *         platform do not support peer-to-peer connections.
     */
    default PeerConnection openPeerConnection(String id) {
        throw new UnsupportedOperationException("Peer-to-peer connections not supported");
    }

    /**
     * Returns true if the application is running in the local development
     * environment. The definition of what such an environment entails depends
     * on the renderer and platform.
     */
    public boolean isDevelopmentEnvironment();
}
