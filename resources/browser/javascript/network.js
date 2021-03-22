//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

class HttpConnection {

    sendGetRequest(url, headers, callback) {
        let request = new XMLHttpRequest();
        request.onreadystatechange = () => {
            if (request.readyState == XMLHttpRequest.DONE) {
                callback(request.responseText);
            }
        };
        request.open("GET", url, true);
        this.prepareRequest(request, headers);
        request.send();
    }

    sendPostRequest(url, headers, params, callback) {
        let request = new XMLHttpRequest();
        request.onreadystatechange = () => {
            if (request.readyState == XMLHttpRequest.DONE) {
                callback(request.responseText);
            }
        };
        request.open("POST", url, true);
        this.prepareRequest(request, headers);
        request.send(params);
    }

    prepareRequest(request, headers) {
        request.setRequestHeader("X-Requested-With", "MultimediaLib");
        for (let i = 0; i < headers.length; i += 2) {
            request.setRequestHeader(headers[i], headers[i + 1]);
        }
    }
}

class WebSocketConnection {

    constructor() {
        this.socket = null;
    }

    connectWebSocket(uri, callback) {
        this.socket = new WebSocket(uri);
        this.socket.onopen = event => callback("__open");
        this.socket.onmessage = event => callback(event.data);
        this.socket.onerror = error => console.log("Web socket error: " + error.message);
    }

    sendWebSocket(message) {
        if (this.socket == null) {
            throw "Web socket not open";
        }

        this.socket.send(message);
    }

    closeWebSocket() {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }
}

class PeerConnection {

    constructor() {
        this.peerConnections = [];
    }

    openPeerConnection(id, receiveCallback) {
        let peer = new Peer();

        peer.on("open", peerId => {
            receiveCallback("__peer:" + peerId);

            if (id != null) {
                let connection = peer.connect(id, {serialization: "json"});
                connection.on("open", () => this.initPeerConnection(connection, receiveCallback));
            } else {
                peer.on("connection", connection => {
                    connection.on("open", () => this.initPeerConnection(connection, receiveCallback));
                });
            }
        });
    }

    initPeerConnection(connection, receiveCallback) {
        let peerConnection = connection;
        this.peerConnections.push(peerConnection);

        peerConnection.on("data", message => {
            this.broadcastPeerMessage(peerConnection, message);
            receiveCallback(message);
        });

        peerConnection.on("close", () => console.warn("Peer connection closed"));
        peerConnection.on("error", error => console.warn("Peer connection error: " + error));

        receiveCallback("__open");
    }

    broadcastPeerMessage(origin, message) {
        for (let peerConnection of this.peerConnections) {
            if (origin.peer != peerConnection.peer) {
                peerConnection.send(message);
            }
        }
    }

    sendPeerMessage(message) {
        for (let peerConnection of this.peerConnections) {
            peerConnection.send(message);
        }
    }

    closePeerConnection() {
        for (let peerConnection of this.peerConnections) {
            peerConnection.close();
        }

        this.peerConnections = [];
    }
}
