//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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
        this.initialized = false;
    }

    open(messageCallback) {
        this.peer = new Peer();
        this.connections = {};
        this.initialized = false;

        this.peer.on("open", peerId => this.handleInit(peerId, messageCallback));
        this.peer.on("connection", connection => this.handleConnect(connection, messageCallback));
        this.peer.on("close", () => console.warn("Peer closed"));
        this.peer.on("disconnected", () => console.warn("Peer disconnected"));
        this.peer.on("error", error => messageCallback("error", error.type));
    }

    handleInit(peerId, messageCallback) {
        this.initialized = true;
        messageCallback("init", peerId);
    }

    handleConnect(connection, messageCallback) {
        this.connections[connection.label] = connection;

        connection.on("open", () => {
            connection.on("data", data => messageCallback("data", data));
            connection.on("close", () => this.handleDisconnect(connection, messageCallback));
            connection.on("error", error => messageCallback("error", error.type));

            messageCallback("connect", connection.label);
        });
    }

    handleDisconnect(connection, messageCallback) {
        delete this.connections[connection.label];
        messageCallback("disconnect", connection.label);
    }

    connect(peerId, messageCallback) {
        const connection = this.peer.connect(peerId);
        this.handleConnect(connection, messageCallback);
    }

    sendMessage(message) {
        for (let connection of Object.values(this.connections)) {
            connection.send(message);
        }
    }

    sendMessageToPeer(peerId, message) {
        if (this.connections[peerId]) {
            this.connections[peerId].send(message);
        }
    }
}
