//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Bridge interface for accessing PeerJS connections. This interface is called
 * from the renderer via TeaVM.
 */
class PeerJsBridge {

    constructor() {
        this.peer = null;
        this.id = null;
        this.connections = [];
        this.receivedMessages = [];
        this.errors = [];
    }

    open(callback) {
        if (this.peer) {
            throw new Error("Peer connection is already open");
        }

        this.peer = new Peer();
        this.peer.on("open", id => this.handleOpen(id, callback));
        this.peer.on("connection", connection => this.handleConnection(connection));
        this.peer.on("close", () => this.id = null);
        this.peer.on("disconnected", () => console.warn("Peer disconnected"));
        this.peer.on("error", error => this.handleError(error));
    }

    handleOpen(id, callback) {
        this.id = id;
        callback(id != null);
        if (!id) {
            console.warn("Failed to open peer connection");
        }
    }

    handleConnection(connection, callback) {
        this.connections.push(connection);

        connection.on("open", () => {
            if (callback) {
                callback(true);
            }

            connection.on("data", message => this.receivedMessages.push(message));
            connection.on("close", () => console.warn("Connection closed"));
            connection.on("error", error => this.handleError(error));
        });
    }

    handleError(error) {
        this.errors.push(error);
        console.warn("Peer connection error: " + error);
    }

    connect(peerId, callback) {
        const connection = this.peer.connect(peerId);
        this.handleConnection(connection, callback);
    }

    sendMessage(message) {
        for (let connection of this.connections) {
            connection.send(message);
        }
    }

    flushReceivedMessages() {
        const flushed = this.receivedMessages;
        this.receivedMessages = [];
        return flushed;
    }

    close() {
        this.peer.destroy();
        this.peer = null;
        this.id = null;
        this.connections = [];
        this.receivedMessages = [];
        this.errors = [];
    }
}
