//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Promise;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLResponse;
import org.teavm.jso.ajax.XMLHttpRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sends HTTP requests by delegating them to JavaScript and sending them as
 * AJAX requests. This does mean that any requests sent from the application
 * must be allowed by the CORS (Cross Origin Resource Sharing) headers returned
 * by the server.
 */
public class TeaNetwork implements Network {

    private static final Splitter HEADER_SPLITTER = Splitter.on(CharMatcher.anyOf("\r\n"));
    private static final Logger LOGGER = LogHelper.getLogger(TeaNetwork.class);

    @Override
    public Promise<URLResponse> get(String url, Headers headers) {
        XMLHttpRequest request = XMLHttpRequest.create();
        Promise<URLResponse> promise = new Promise<>();
        request.setOnReadyStateChange(() -> handleResponse(request, promise));
        request.open("GET", url, true);
        addRequestHeaders(request, headers);
        request.send();
        return promise;
    }

    @Override
    public Promise<URLResponse> post(String url, Headers headers, PostData data) {
        XMLHttpRequest request = XMLHttpRequest.create();
        Promise<URLResponse> promise = new Promise<>();
        request.setOnReadyStateChange(() -> handleResponse(request, promise));
        request.open("POST", url, true);
        addRequestHeaders(request, headers);
        request.send(data.encode(UTF_8));
        return promise;
    }

    private void addRequestHeaders(XMLHttpRequest request, Headers headers) {
        request.setRequestHeader("X-Requested-With", "MultimediaLib");
        if (headers != null) {
            headers.forEach(request::setRequestHeader);
        }
    }

    private void handleResponse(XMLHttpRequest request, Promise<URLResponse> promise) {
        if (request.getReadyState() == XMLHttpRequest.DONE) {
            if (request.getStatus() >= 200 && request.getStatus() <= 204) {
                URLResponse response = parseResponse(request);
                promise.resolve(response);
            } else {
                promise.reject(new IOException("AJAX request failed: " + request.getStatusText()));
            }
        }
    }

    private URLResponse parseResponse(XMLHttpRequest request) {
        int status = request.getStatus();
        Headers headers = parseResponseHeaders(request);
        String body = request.getResponseText();
        return new URLResponse(status, headers, body.getBytes(UTF_8), UTF_8, Collections.emptyMap());
    }

    private Headers parseResponseHeaders(XMLHttpRequest request) {
        Headers headers = new Headers();

        for (String line : HEADER_SPLITTER.split(request.getAllResponseHeaders())) {
            if (line.contains(": ")) {
                String name = line.substring(0, line.indexOf(": "));
                String value = line.substring(line.indexOf(": ") + 2);
                headers = headers.concat(name, value);
            } else if (!line.isEmpty()) {
                LOGGER.warning("Malformed HTTP response header: " + line);
            }
        }

        return headers;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return BrowserDOM.getQueryString().contains("local");
    }
}
