//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.multimedialib.renderer.Response;
import nl.colorize.util.EventQueue;
import nl.colorize.util.LogHelper;
import nl.colorize.util.http.HttpException;
import nl.colorize.util.stats.TupleList;
import org.jspecify.annotations.Nullable;
import org.teavm.jso.ajax.XMLHttpRequest;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Sends HTTP requests by delegating them to JavaScript and sending them as
 * AJAX requests. This does mean that any requests sent from the application
 * must be allowed by the CORS (Cross Origin Resource Sharing) headers
 * returned by the server.
 * <p>
 * Peer-to-peer connections are supported using WebRTC. This is mostly
 * implemented in JavaScript using the <a href="https://peerjs.com">PeerJS</a>
 * library.
 */
public class TeaNetwork implements Network {

    private static final Splitter HEADER_SPLITTER = Splitter.on(CharMatcher.anyOf("\r\n"));
    private static final Logger LOGGER = LogHelper.getLogger(TeaNetwork.class);

    @Override
    public EventQueue<Response> send(
        String method,
        String url,
        Map<String, String> headers,
        @Nullable String body
    ) {
        XMLHttpRequest request = XMLHttpRequest.create();
        EventQueue<Response> eventQueue = new EventQueue<>();
        request.setOnReadyStateChange(() -> handleResponse(request, eventQueue));
        request.open(method, url, true);
        addRequestHeaders(request, headers);
        if (body != null && !body.isEmpty()) {
            request.send(body);
        } else {
            request.send();
        }
        return eventQueue;
    }

    private void addRequestHeaders(XMLHttpRequest request, Map<String, String> headers) {
        request.setRequestHeader("X-Requested-With", "MultimediaLib");
        headers.forEach(request::setRequestHeader);
    }

    private void handleResponse(XMLHttpRequest request, EventQueue<Response> eventQueue) {
        if (request.getReadyState() == XMLHttpRequest.DONE) {
            if (request.getStatus() >= 200 && request.getStatus() <= 204) {
                eventQueue.onNext(mapResponse(request));
            } else {
                HttpException error = new HttpException(
                    "AJAX request failed: " + request.getStatusText(), request.getStatus());
                eventQueue.onError(error);
            }
        }
    }

    private Response mapResponse(XMLHttpRequest request) {
        int status = request.getStatus();
        TupleList<String, String> headers = new TupleList<>();
        String body = request.getResponseText();

        for (String line : HEADER_SPLITTER.split(request.getAllResponseHeaders())) {
            if (line.contains(": ")) {
                String name = line.substring(0, line.indexOf(": "));
                String value = line.substring(line.indexOf(": ") + 2);
                headers.add(name, value);
            } else if (!line.isEmpty()) {
                LOGGER.warning("Malformed HTTP response header: " + line);
            }
        }

        return new Response(status, headers, body);
    }

    @Override
    public PeerConnection openPeerConnection() {
        PeerjsBridge bridge = Browser.getPeerJsBridge();
        return new PeerjsConnection(bridge);
    }

    @Override
    public boolean isPeerToPeerSupported() {
        return true;
    }
}
