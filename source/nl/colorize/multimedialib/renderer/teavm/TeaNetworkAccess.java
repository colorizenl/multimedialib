//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.NetworkAccess;
import nl.colorize.multimedialib.renderer.NetworkConnection;
import nl.colorize.util.Callback;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Tuple;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.websocket.WebSocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

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

    private static final Logger LOGGER = LogHelper.getLogger(TeaNetworkAccess.class);

    @Override
    public void get(String url, Headers headers, Callback<String> callback) {
        XMLHttpRequest request = XMLHttpRequest.create();
        request.setOnReadyStateChange(() -> handleResponse(request, callback));
        request.open("GET", url, true);
        addRequestHeaders(request, headers);
        request.send();
    }

    @Override
    public void post(String url, Headers headers, PostData data, Callback<String> callback) {
        XMLHttpRequest request = XMLHttpRequest.create();
        request.setOnReadyStateChange(() -> handleResponse(request, callback));
        request.open("POST", url, true);
        addRequestHeaders(request, headers);
        request.send(data.encode(StandardCharsets.UTF_8));
    }

    private void addRequestHeaders(XMLHttpRequest request, Headers headers) {
        request.setRequestHeader("X-Requested-With", "MultimediaLib");
        for (Tuple<String, String> header : headers.getEntries()) {
            request.setRequestHeader(header.getKey(), header.getValue());
        }
    }

    private void handleResponse(XMLHttpRequest request, Callback<String> callback) {
        if (request.getReadyState() == XMLHttpRequest.DONE) {
            if (request.getStatus() >= 200 && request.getStatus() <= 204) {
                String response = request.getResponseText();
                callback.onResponse(response);
            } else {
                callback.onError(new IOException("AJAX request failed: " + request.getStatusText()));
            }
        }
    }

    @Override
    public NetworkConnection connectWebSocket(String uri) {
        WebSocket webSocket = WebSocket.create(uri);
        NetworkConnection connection = new NetworkConnection(message -> webSocket.send(message));

        webSocket.onOpen(message -> {
            connection.receiveID(uri);
            connection.connect();
        });
        webSocket.onMessage(message -> connection.queueReceivedMessage(message.getDataAsString()));
        webSocket.onError(event -> LOGGER.warning("Web socket error"));

        return connection;
    }

    @Override
    public NetworkConnection connectWebRTC(String id) {
        throw new UnsupportedOperationException();
    }
}
