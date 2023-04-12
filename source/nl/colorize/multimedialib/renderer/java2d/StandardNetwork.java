//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.util.Platform;
import nl.colorize.util.Promise;
import nl.colorize.util.http.Headers;
import nl.colorize.util.http.Method;
import nl.colorize.util.http.PostData;
import nl.colorize.util.http.URLLoader;
import nl.colorize.util.http.URLResponse;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Sends HTTP requests using the HTTP client included as part of the Java
 * standard library. It also provides web socket support through the
 * Java-WebSocket library.
 */
public class StandardNetwork implements Network {

    @Override
    public Promise<URLResponse> get(String url, Headers headers) {
        URLLoader request = createRequest(Method.GET, url, headers, null);
        return request.sendPromise();
    }

    @Override
    public Promise<URLResponse> post(String url, Headers headers, PostData body) {
        URLLoader request = createRequest(Method.POST, url, headers, body);
        return request.sendPromise();
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
    public boolean isDevelopmentEnvironment() {
        File workDir = Platform.getUserWorkingDirectory();
        return new File(workDir, "build.gradle").exists();
    }
}
