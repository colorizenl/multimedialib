//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.util.Subject;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.Method;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLLoader;
import nl.colorize.util.http.URLResponse;

import java.nio.charset.StandardCharsets;

/**
 * Sends HTTP requests using the HTTP client included as part of the Java
 * standard library. It also provides web socket support through the
 * Java-WebSocket library.
 */
public class StandardNetwork implements Network {

    @Override
    public Subject<URLResponse> get(String url, Headers headers) {
        URLLoader request = createRequest(Method.GET, url, headers, null);
        return request.sendBackground();
    }

    @Override
    public Subject<URLResponse> post(String url, Headers headers, PostData body) {
        URLLoader request = createRequest(Method.POST, url, headers, body);
        return request.sendBackground();
    }

    private URLLoader createRequest(Method method, String url, Headers headers, PostData body) {
        URLLoader request = new URLLoader(method, url, StandardCharsets.UTF_8);
        request.withHeader(HttpHeaders.X_REQUESTED_WITH, "MultimediaLib");
        if (headers != null) {
            request.withHeaders(headers);
        }
        if (body != null) {
            request.withBody(body);
        }
        return request;
    }

    @Override
    public PeerConnection openPeerConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPeerToPeerSupported() {
        return false;
    }
}
