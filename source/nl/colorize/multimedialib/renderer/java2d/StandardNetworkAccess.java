//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.NetworkConnection;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Task;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.Method;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLLoader;
import nl.colorize.util.http.URLResponse;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends HTTP requests using the HTTP client included as part of the Java
 * standard library. It also provides web socket support through the
 * Java-WebSocket library.
 */
public class StandardNetworkAccess implements NetworkAccess {

    private static final Logger LOGGER = LogHelper.getLogger(StandardNetworkAccess.class);

    @Override
    public Task<String> get(String url, Headers headers) {
        return send(Method.GET, url, headers, null);
    }

    @Override
    public Task<String> post(String url, Headers headers, PostData body) {
        return send(Method.POST, url, headers, body);
    }

    public Task<String> send(Method method, String url, Headers headers, PostData body) {
        URLLoader request = URLLoader.create(method, url, StandardCharsets.UTF_8);
        request.addHeader(HttpHeaders.X_REQUESTED_WITH, "MultimediaLib");
        if (headers != null) {
            request.addHeaders(headers);
        }

        if (body != null) {
            request.setBody(body);
        }

        return request.sendBackgroundRequest().pipe(URLResponse::getBody);
    }

    @Override
    public boolean isWebSocketSupported() {
        return true;
    }

    @Override
    public NetworkConnection connectWebSocket(String uri) {
        JavaWebSocketClient client = new JavaWebSocketClient(URI.create(uri));
        client.connect();
        return client.connection;
    }

    @Override
    public boolean isWebRtcSupported() {
        return false;
    }

    @Override
    public NetworkConnection connectWebRTC(String id) {
        throw new UnsupportedOperationException("WebRTC not supported by platform " +
            Platform.getPlatformName());
    }

    /**
     * Implementation of web socket client using the Java-WebSocket library.
     * Note that the client will run in a separate thread, which is different
     * from the renderer thread.
     */
    private static class JavaWebSocketClient extends WebSocketClient {

        private NetworkConnection connection;

        public JavaWebSocketClient(URI uri) {
            super(uri);
            this.connection = new NetworkConnection(this::send);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            LOGGER.info("Web socket connection: " + handshake.getHttpStatusMessage());
            connection.receiveID(UUID.randomUUID().toString());
            connection.connect();
            send("__init");
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            LOGGER.info("Web socket connection closed");
            connection.disconnect();
        }

        @Override
        public void onMessage(String message) {
            connection.queueReceivedMessage(message);
        }

        @Override
        public void onMessage(ByteBuffer buffer) {
            String message = new String(buffer.array(), Charsets.UTF_8);
            connection.queueReceivedMessage(message);
        }

        @Override
        public void onError(Exception e) {
            LOGGER.log(Level.WARNING, "Web socket error", e);
        }
    }
}
