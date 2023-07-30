//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

if (!("randomUUID" in window.crypto)) {
    window.crypto.randomUUID = function() {
        const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
        return s4() + s4() + "-" + s4() + "-" + s4() + "-" + s4() + "-" + s4() + s4() + s4();
    };
}

document.addEventListener("DOMContentLoaded", event => {
    window.addEventListener("touchstart", handleTouchEvent, true);
    window.addEventListener("touchmove", handleTouchEvent, true);
    window.addEventListener("touchend", handleTouchEvent, true);
    window.addEventListener("touchcancel", handleTouchEvent, true);

    window.pixiInterface = new PixiInterface();
    window.threeInterface = new ThreeInterface();

    // TeaVM entry point in transpiled code.
    main();
});

window.prepareAnimationLoop = function() {
    const spinner = document.getElementById("loading");
    spinner.style.display = "none";
}

window.registerErrorHandler = function(callback) {
    window.addEventListener("error", event => {
        console.error("JavaScript error" + event.error);

        if (event.error && event.error.stack) {
            const stackTrace = event.error.stack.replace(/@file\S+/g, "").substring(0, 256);
            callback(event.error.message + "\n\n" + stackTrace);
        } else {
            callback(event.message);
        }
    });
}

window.preloadFontFace = function(family, url, callback) {
    const fontFace = new FontFace(family, url, {
        style: "normal",
        weight: "normal"
    });

    document.fonts.add(fontFace);

    fontFace.load()
        .then(result => callback(true))
        .catch(error => callback(false));
}

/**
 * TeaVM does not yet provide bindings to touch events, so we have to convert
 * them to mouse events in order to support touch.
 */
function handleTouchEvent(touchEvent) {
    const eventMapping = {
        touchstart: "mousedown",
        touchmove: "mousemove",
        touchend: "mouseup"
    };

    const type = eventMapping[touchEvent.type];

    if (type) {
        const touch = touchEvent.changedTouches[0];
        const simulatedEvent = document.createEvent("MouseEvent");
        simulatedEvent.initMouseEvent(type, true, true, window, 1,
            touch.screenX, touch.screenY, touch.clientX, touch.clientY,
            false, false, false, false, 0, null);

        touch.target.dispatchEvent(simulatedEvent);
        touchEvent.preventDefault();
    }
}
