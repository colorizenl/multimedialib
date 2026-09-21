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
        this.connections[connection.label] = connection;

        connection.on("open", () => {
            connection.on("data", data => this.notifyCallbacks(connection.label, data));
            connection.on("close", () => this.handleDisconnect(connection));
            connection.on("error", error => this.handleError(error));

            this.notifyCallbacks(connection.label, "$$connect");
        });
    }

    handleError(connection, error) {
        console.warn("Peer connection error: " + error.type);
        this.notifyCallbacks(connection.label, "$$error");
    }

    handleDisconnect(connection) {
        delete this.connections[connection.label];
        this.notifyCallbacks(connection.label, "$$disconnect");
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
