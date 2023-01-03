//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.util.Callback;
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
    public void get(String url, Headers headers, Callback<URLResponse> callback) {
        URLLoader request = createRequest(Method.GET, url, headers, null);
        request.sendAsync(callback);
    }

    @Override
    public void post(String url, Headers headers, PostData body, Callback<URLResponse> callback) {
        URLLoader request = createRequest(Method.POST, url, headers, body);
        request.sendAsync(callback);
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
}
