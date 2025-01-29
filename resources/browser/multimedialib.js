//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

import {BrowserBridge} from "./browser-bridge.js";
import {PeerJsBridge} from "./peerjs-bridge.js";
import {PixiBridge} from "./pixi-bridge.js";
import {ThreeBridge} from "./three-bridge.js";

function polyfill() {
    if (!("randomUUID" in window.crypto)) {
        window.crypto.randomUUID = function() {
            const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
            return s4() + s4() + "-" + s4() + "-" + s4() + "-" + s4() + "-" + s4() + s4() + s4();
        };
    }
}

export function bootstrap() {
    polyfill();

    window.browserBridge = new BrowserBridge();
    window.pixiBridge = new PixiBridge();
    window.threeBridge = new ThreeBridge();
    window.peerjsBridge = new PeerJsBridge();

    // TeaVM entry point in transpiled code.
    main();
}
