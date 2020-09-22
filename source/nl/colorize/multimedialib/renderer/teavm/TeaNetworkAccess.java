//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.NetworkConnection;
import nl.colorize.util.Task;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends HTTP requests by delegating them to JavaScript and sending them as
 * AJAX requests. This does mean that any requests sent from the application
 * must be allowed by the CORS (Cross Origin Resource Sharing) headers returned
 * by the server.
 * <p>
 * Web Sockets are also supported through the JavaScript web socket API, but
 * this is only supported when running in browsers that include this API.
 */
public class TeaNetworkAccess implements NetworkAccess {

    @Override
    public Task<String> get(String url, Headers headers) {
        String[] headersArray = serializeHeaders(headers);
        Task<String> promise = new Task<>();
        Browser.sendGetRequest(url, headersArray, promise::complete);
        return promise;
    }

    @Override
    public Task<String> post(String url, Headers headers, PostData data) {
        String[] headersArray = serializeHeaders(headers);
        String body = data.encode(StandardCharsets.UTF_8);
        Task<String> promise = new Task<>();
        Browser.sendPostRequest(url, headersArray, body, promise::complete);
        return promise;
    }

    private String[] serializeHeaders(Headers headers) {
        List<String> entries = new ArrayList<>();

        if (headers != null) {
            for (String name : headers.getNames()) {
                for (String value : headers.getValues(name)) {
                    entries.add(name);
                    entries.add(value);
                }
            }
        }

        return entries.toArray(new String[0]);
    }

    @Override
    public boolean isWebSocketSupported() {
        return Browser.isWebSocketSupported();
    }

    @Override
    public NetworkConnection connectWebSocket(String uri) {
        NetworkConnection connection = new NetworkConnection(Browser::sendWebSocket);

        Browser.connectWebSocket(uri, message -> {
            if (message.equals("__open")) {
                connection.receiveID(uri);
                connection.connect();
            } else {
                connection.queueReceivedMessage(message);
            }
        });

        return connection;
    }

    @Override
    public boolean isWebRtcSupported() {
        return false;
    }

    @Override
    public NetworkConnection connectWebRTC(String id) {
        NetworkConnection connection = new NetworkConnection(Browser::sendPeerMessage);

        Browser.openPeerConnection(id, message -> {
            if (message.startsWith("__peer:")) {
                connection.receiveID(message.substring(7));
            } else if (message.equals("__open")) {
                connection.connect();
            } else {
                connection.queueReceivedMessage(message);
            }
        });

        return connection;
    }
}
