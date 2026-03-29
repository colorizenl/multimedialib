//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

function polyfill() {
    if (!("randomUUID" in window.crypto)) {
        window.crypto.randomUUID = function() {
            return "10000000-1000-4000-8000-100000000000".replace(/[018]/g, c =>
                (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
            );
        }
    }
}

function bootstrap() {
    polyfill();
    window.browserBridge = new BrowserBridge();
    window.peerjsBridge = new PeerJsBridge();
    // TeaVM entry point in transpiled code.
    main();
}
