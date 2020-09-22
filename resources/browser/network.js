//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

let socket = null;
let peerConnections = [];

function sendGetRequest(url, headers, callback) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = () => {
        if (request.readyState == XMLHttpRequest.DONE) {
            callback(request.responseText);
        }
    };
    request.open("GET", url, true);
    prepareRequest(request, headers);
    request.send();
}

function sendPostRequest(url, headers, params, callback) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = () => {
        if (request.readyState == XMLHttpRequest.DONE) {
            callback(request.responseText);
        }
    };
    request.open("POST", url, true);
    prepareRequest(request, headers);
    request.send(params);
}

function prepareRequest(request, headers) {
    request.setRequestHeader("X-Requested-With", "MultimediaLib");
    for (let i = 0; i < headers.length; i += 2) {
        request.setRequestHeader(headers[i], headers[i + 1]);
    }
}

function connectWebSocket(uri, callback) {
    socket = new WebSocket(uri);
    socket.onopen = event => callback("__open");
    socket.onmessage = event => callback(event.data);
    socket.onerror = error => console.log("Web socket error: " + error.message);
}

function sendWebSocket(message) {
    if (socket == null) {
        throw "Web socket not open";
    }

    socket.send(message);
}

function closeWebSocket() {
    if (socket != null) {
        socket.close();
        socket = null;
    }
}

function openPeerConnection(id, receiveCallback) {
    let peer = new Peer();

    peer.on("open", peerId => {
        receiveCallback("__peer:" + peerId);

        if (id != null) {
            let connection = peer.connect(id, {serialization: "json"});
            connection.on("open", () => initPeerConnection(connection, receiveCallback));
        } else {
            peer.on("connection", connection => {
                connection.on("open", () => initPeerConnection(connection, receiveCallback));
            });
        }
    });
}

function initPeerConnection(connection, receiveCallback) {
    let peerConnection = connection;
    peerConnections.push(peerConnection);

    peerConnection.on("data", message => {
        broadcastPeerMessage(peerConnection, message);
        receiveCallback(message);
    });

    peerConnection.on("close", () => console.warn("Peer connection closed"));
    peerConnection.on("error", error => console.warn("Peer connection error: " + error));

    receiveCallback("__open");
}

function broadcastPeerMessage(origin, message) {
    for (let peerConnection of peerConnections) {
        if (origin.peer != peerConnection.peer) {
            peerConnection.send(message);
        }
    }
}

function sendPeerMessage(message) {
    for (let peerConnection of peerConnections) {
        peerConnection.send(message);
    }
}

function closePeerConnection() {
    for (let peerConnection of peerConnections) {
        peerConnection.close();
    }

    peerConnections = [];
}
