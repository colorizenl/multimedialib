//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Bridge interface for accessing PeerJS connections. This interface is called
 * from the renderer via TeaVM.
 */
class PeerJsBridge {

    constructor() {
        this.peer = null;
        this.connections = {};
        this.callbacks = [];
    }

    registerCallback(callback) {
        this.callbacks.push(callback);
    }

    notifyCallbacks(id, message) {
        for (let callback of this.callbacks) {
            callback(id, message);
        }
    }

    open() {
        this.peer = new Peer();
        this.connections = {};

        this.peer.on("open", peerId => this.notifyCallbacks(peerId, "$$open"));
        this.peer.on("connection", connection => this.handleConnect(connection));
        this.peer.on("close", () => console.error("Peer closed"));
        this.peer.on("disconnected", () => console.error("Peer disconnected"));
        this.peer.on("error", error => this.handleError(error));
    }

    handleConnect(connection) {
        this.connections[connection.peer] = connection;

        connection.on("open", () => {
            console.log("Peer connection opened: " + connection.peer)
            connection.on("data", data => this.handleMessage(connection, data));
            connection.on("close", () => this.handleDisconnect(connection));
            connection.on("error", error => this.handleError(error));

            this.notifyCallbacks(connection.peer, "$$connect");
        });
    }

    handleMessage(connection, data) {
        if (data.startsWith("$$")) {
            console.warn("Invalid peer message: " + data);
            return;
        }

        this.notifyCallbacks(connection.peer, data);
    }

    handleError(connection, error) {
        console.warn("Peer connection error: " + error);
        this.notifyCallbacks(connection.peer, "$$error");
    }

    handleDisconnect(connection) {
        delete this.connections[connection.peer];
        this.notifyCallbacks(connection.peer, "$$disconnect");
    }

    join(peerId) {
        const connection = this.peer.connect(peerId);
        this.handleConnect(connection);
    }

    send(message) {
        for (let connection of Object.values(this.connections)) {
            connection.send(message);
        }
    }

    getPeerConnectionIds() {
        return Object.keys(this.connections);
    }

    isConnectionInitialized() {
        return this.peer != null;
    }
}
