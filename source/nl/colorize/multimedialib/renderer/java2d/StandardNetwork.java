//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Joiner;
import com.google.common.net.HttpHeaders;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.util.EventQueue;
import nl.colorize.util.Platform;
import nl.colorize.util.http.URLLoader;
import org.jspecify.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Sends HTTP requests using the HTTP client included as part of the Java
 * standard library. It also provides web socket support through the
 * Java-WebSocket library.
 */
public class StandardNetwork implements Network {

    private static final Joiner HEADER_JOINER = Joiner.on(", ");

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
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
            String value = HEADER_JOINER.join(header.getValue());
            headers.put(header.getKey(), value);
        }

        return new Response(response.statusCode(), headers, response.body());
    }

    @Override
    public boolean isPeerToPeerSupported() {
        return false;
    }

    @Override
    public EventQueue<PeerMessage> openPeerConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventQueue<PeerMessage> joinPeerConnection(String peerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPeerConnection(String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getPeerConnectionIds() {
        throw new UnsupportedOperationException();
    }
}
