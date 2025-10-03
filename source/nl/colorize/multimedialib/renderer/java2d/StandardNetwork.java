//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.PeerConnection;
import nl.colorize.multimedialib.renderer.Response;
import nl.colorize.util.EventQueue;
import nl.colorize.util.Platform;
import nl.colorize.util.http.URLLoader;
import nl.colorize.util.stats.TupleList;
import org.jspecify.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends HTTP requests using the HTTP client included as part of the Java
 * standard library. It also provides web socket support through the
 * Java-WebSocket library.
 */
public class StandardNetwork implements Network {

    @Override
    public EventQueue<Response> send(
        String method,
        String url,
        Map<String, String> headers,
        @Nullable String body
    ) {
        Map<String, String> combinedHeaders = new LinkedHashMap<>();
        combinedHeaders.put("X-Colorize-Platform", Platform.getPlatformName());
        combinedHeaders.put(HttpHeaders.X_REQUESTED_WITH, "MultimediaLib");
        combinedHeaders.putAll(headers);

        HttpRequest request = URLLoader.buildRequest(method, url, combinedHeaders, body);
        EventQueue<Response> eventQueue = new EventQueue<>();
        URLLoader.sendAsync(request)
            .map(this::mapResponse)
            .subscribe(eventQueue::onNext, eventQueue::onError);
        return eventQueue;
    }

    private Response mapResponse(HttpResponse<String> response) {
        TupleList<String, String> headers = new TupleList<>();

        for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
            for (String value : header.getValue()) {
                headers.add(header.getKey(), value);
            }
        }

        return new Response(response.statusCode(), headers, response.body());
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
