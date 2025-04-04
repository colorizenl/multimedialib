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
import nl.colorize.util.LogHelper;
import nl.colorize.util.Subject;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLResponse;
import nl.colorize.util.stats.TupleList;
import org.teavm.jso.ajax.XMLHttpRequest;

import java.io.IOException;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    public Subject<URLResponse> get(String url, Headers headers) {
        XMLHttpRequest request = XMLHttpRequest.create();
        Subject<URLResponse> subject = new Subject<>();
        request.setOnReadyStateChange(() -> handleResponse(request, subject));
        request.open("GET", url, true);
        addRequestHeaders(request, headers);
        request.send();
        return subject;
    }

    @Override
    public Subject<URLResponse> post(String url, Headers headers, PostData data) {
        XMLHttpRequest request = XMLHttpRequest.create();
        Subject<URLResponse> subject = new Subject<>();
        request.setOnReadyStateChange(() -> handleResponse(request, subject));
        request.open("POST", url, true);
        addRequestHeaders(request, headers);
        request.send(data.encode(UTF_8));
        return subject;
    }

    private void addRequestHeaders(XMLHttpRequest request, Headers headers) {
        request.setRequestHeader("X-Requested-With", "MultimediaLib");
        if (headers != null) {
            headers.forEach(request::setRequestHeader);
        }
    }

    private void handleResponse(XMLHttpRequest request, Subject<URLResponse> subject) {
        if (request.getReadyState() == XMLHttpRequest.DONE) {
            if (request.getStatus() >= 200 && request.getStatus() <= 204) {
                URLResponse response = parseResponse(request);
                subject.next(response);
            } else {
                subject.nextError(new IOException("AJAX request failed: " + request.getStatusText()));
            }
        }
    }

    private URLResponse parseResponse(XMLHttpRequest request) {
        int status = request.getStatus();
        Headers headers = parseResponseHeaders(request);
        String body = request.getResponseText();
        return new URLResponse(status, headers, body.getBytes(UTF_8));
    }

    private Headers parseResponseHeaders(XMLHttpRequest request) {
        TupleList<String, String> headers = new TupleList<>();

        for (String line : HEADER_SPLITTER.split(request.getAllResponseHeaders())) {
            if (line.contains(": ")) {
                String name = line.substring(0, line.indexOf(": "));
                String value = line.substring(line.indexOf(": ") + 2);
                headers.add(name, value);
            } else if (!line.isEmpty()) {
                LOGGER.warning("Malformed HTTP response header: " + line);
            }
        }

        return new Headers(headers);
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
