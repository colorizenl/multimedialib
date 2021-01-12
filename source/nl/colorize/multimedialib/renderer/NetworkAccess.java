//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.util.Task;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

/**
 * Interface for the platform-specific mechanism used to access the internet.
 * To prevent blocking the application, HTTP requests are sent asynchronous
 * and a callback function is invoked once the response has been received.
 * <p>
 * Note that only a limited subset of HTTP requests is supported, as not all
 * features to customize requests are supported on all platforms. Moreover,
 * the response is always returned as plain text. Parsing or mapping the
 * response, for example from JSON to Java classes, tends to rely on reflection
 * which is not available on some platforms. Applications must therefore parse
 * the response manually.
 * <p>
 * Requests sent using this class will add the
 * {@code X-Requested-With: MultimediaLib} header. This can be used by the
 * server to identify requests originating from MultimediaLib applications.
 * <p>
 * In addition to regular HTTP requests, this class also supports web socket
 * connections. However, unlike regular HTTP requests this may not be supported
 * on all platforms.
 */
public interface NetworkAccess {

    /**
     * Sends an asynchronous HTTP GET request to the specified URL.
     */
    public Task<String> get(String url, Headers headers);

    /**
     * Sends am asynchronous HTTP POST request to the specified URL. The request
     * body will be encoded using the {@code application/x-www-form-urlencoded}
     * content type.
     */
    public Task<String> post(String url, Headers headers, PostData body);

    public boolean isWebSocketSupported();

    /**
     * Connects to the web socket located at the specified URI. Note that some
     * platforms may only allow the secure web socket protocol
     * (i.e. {@code "wss://"}).
     *
     * @throws UnsupportedOperationException if the platform does not support
     *         web sockets.
     */
    public NetworkConnection connectWebSocket(String uri);

    public boolean isWebRtcSupported();

    /**
     * Opens a peer-to-peer connection using WebRT. The connection will be created
     * to the peer with the specified ID. This ID must be provided by a broker
     * service, it is not possible to discover peers directly.
     *
     * @throws UnsupportedOperationException if the platform does not support
     *         WebRTC peer-to-peer connections or WebRTC in general.
     */
    public NetworkConnection connectWebRTC(String id);
}
