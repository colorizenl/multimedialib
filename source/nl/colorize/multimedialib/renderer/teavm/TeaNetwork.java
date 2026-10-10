//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.scene.EventSource;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Subject;
import nl.colorize.util.http.HttpException;
import org.jspecify.annotations.Nullable;
import org.teavm.jso.ajax.XMLHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static nl.colorize.multimedialib.renderer.Network.PeerMessage.CONNECT;
import static nl.colorize.multimedialib.renderer.Network.PeerMessage.DISCONNECT;
import static nl.colorize.multimedialib.renderer.Network.PeerMessage.ERROR;
import static nl.colorize.multimedialib.renderer.Network.PeerMessage.OPEN;

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

    private PeerjsBridge peerBridge;
    private List<String> peerConnectBuffer;
    private List<String> peerSendBuffer;
    private Subject<PeerMessage> peerReceiveBuffer;

    private static final Splitter HEADER_SPLITTER = Splitter.on(CharMatcher.anyOf("\r\n"));
    private static final List<String> SYSTEM_MESSAGES = List.of(OPEN, CONNECT, DISCONNECT, ERROR);
    private static final Logger LOGGER = LogHelper.getLogger(TeaNetwork.class);

    public TeaNetwork() {
        this.peerConnectBuffer = new CopyOnWriteArrayList<>();
        this.peerSendBuffer = new CopyOnWriteArrayList<>();
        this.peerReceiveBuffer = new Subject<>();
    }

    @Override
    public EventSource<Response> send(
        String method,
        String url,
        Map<String, String> headers,
        @Nullable String body
    ) {
        XMLHttpRequest request = XMLHttpRequest.create();
        Subject<Response> events = new Subject<>();
        request.setOnReadyStateChange(() -> handleResponse(request, events));
        request.open(method, url, true);
        addRequestHeaders(request, headers);
        if (body != null && !body.isEmpty()) {
            request.send(body);
        } else {
            request.send();
        }
        return EventSource.from(events);
    }

    private void addRequestHeaders(XMLHttpRequest request, Map<String, String> headers) {
        request.setRequestHeader("X-Requested-With", "MultimediaLib");
        headers.forEach(request::setRequestHeader);
    }

    private void handleResponse(XMLHttpRequest request, Subject<Response> events) {
        if (request.getReadyState() == XMLHttpRequest.DONE) {
            if (request.getStatus() >= 200 && request.getStatus() <= 204) {
                events.next(mapResponse(request));
            } else {
                HttpException error = new HttpException(
                    "AJAX request failed: " + request.getStatusText(), request.getStatus());
                events.nextError(error);
            }
        }
    }

    private Response mapResponse(XMLHttpRequest request) {
        int status = request.getStatus();
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String body = request.getResponseText();

        for (String line : HEADER_SPLITTER.split(request.getAllResponseHeaders())) {
            if (line.contains(": ")) {
                String name = line.substring(0, line.indexOf(": "));
                String value = line.substring(line.indexOf(": ") + 2);

                if (headers.containsKey(name)) {
                    headers.put(name, headers.get(name) + ", " + value);
                } else {
                    headers.put(name, value);
                }
            } else if (!line.isEmpty()) {
                LOGGER.warning("Malformed HTTP response header: " + line);
            }
        }

        return new Response(status, headers, body);
    }

    @Override
    public boolean isPeerToPeerSupported() {
        return true;
    }

    private void initPeerConnection() {
        if (peerBridge == null) {
            peerBridge = Browser.getPeerJsBridge();
            peerBridge.registerCallback(this::handlePeerMessage);
            peerBridge.open();
        }
    }

    @Override
    public EventSource<PeerMessage> openPeerConnection() {
        initPeerConnection();
        return EventSource.from(peerReceiveBuffer);
    }

    @Override
    public EventSource<PeerMessage> joinPeerConnection(String peerId) {
        peerConnectBuffer.add(peerId);
        return openPeerConnection();
    }

    private void handlePeerMessage(String id, String message) {
        if (message.startsWith("$$") && !SYSTEM_MESSAGES.contains(message)) {
            LOGGER.warning("Ignoring peer-to-peer message: " + message);
            return;
        }

        if (message.equals(OPEN)) {
            peerConnectBuffer.forEach(peerBridge::join);
            peerConnectBuffer.clear();
        } else if (message.equals(CONNECT)) {
            peerSendBuffer.forEach(peerBridge::send);
            peerSendBuffer.clear();
        }

        PeerMessage peerMessage = new PeerMessage(id, message);
        peerReceiveBuffer.next(peerMessage);
    }

    @Override
    public void sendPeerConnection(String message) {
        Preconditions.checkState(peerBridge != null, "Peer-to-peer connection not active");
        Preconditions.checkArgument(!message.startsWith("$$"), "Invalid peer-to-peer message");

        if (peerBridge.isConnectionInitialized() && peerBridge.getPeerConnectionIds().length > 0) {
            peerBridge.send(message);
        } else {
            peerSendBuffer.add(message);
        }
    }

    @Override
    public Set<String> getPeerConnectionIds() {
        Preconditions.checkState(peerBridge != null, "Peer-to-peer connection not active");
        return Set.of(peerBridge.getPeerConnectionIds());
    }
}
