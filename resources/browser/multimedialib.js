//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

if (!("randomUUID" in window.crypto)) {
    window.crypto.randomUUID = function() {
        const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
        return s4() + s4() + "-" + s4() + "-" + s4() + "-" + s4() + "-" + s4() + s4() + s4();
    };
}

window.addEventListener("load", event => {
    const container = document.getElementById("multimediaLibContainer");
    container.addEventListener("touchstart", handleTouchEvent, true);
    container.addEventListener("touchmove", handleTouchEvent, true);
    container.addEventListener("touchend", handleTouchEvent, true);
    container.addEventListener("touchcancel", handleTouchEvent, true);

    window.browserBridge = new BrowserBridge();
    window.pixiBridge = new PixiBridge();
    window.threeBridge = new ThreeBridge();
    window.peerjsBridge = new PeerJsBridge();

    // TeaVM entry point in transpiled code.
    main();
});

/**
 * TeaVM does not yet provide bindings for touch events, so this generates
 * custom events based on the original touch events. These custom events
 * are then processed in order to support touch input.
 */
function handleTouchEvent(touchEvent) {
    for (let i = 0; i < touchEvent.changedTouches.length; i++) {
        const customEvent = new CustomEvent("custom:" + touchEvent.type, {
            bubbles: false,
            cancelable: true,
            composed: false,
            detail: {
                identifier: touchEvent.changedTouches[i].identifier,
                pageX: touchEvent.changedTouches[i].clientX,
                pageY: touchEvent.changedTouches[i].clientY
            }
        });

        touchEvent.target.dispatchEvent(customEvent);
    }

    touchEvent.preventDefault();
}
