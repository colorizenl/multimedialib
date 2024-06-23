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

/**
 * Called when the initialization process has been completed. Called from the
 * animation loop via TeaVM.
 */
window.prepareAnimationLoop = function() {
    const spinner = document.getElementById("loading");
    spinner.style.display = "none";
}

/**
 * Uses the specified callback function to bridge JavaScript errors to TeaVM.
 * Called from the animation loop via TeaVM.
 */
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

/**
 * Registers a TrueType font. The callback function receives a boolean that
 * indicates whether the font was loaded successfully. Called from the
 * animation loop via TeaVM.
 */
window.preloadFontFace = function(family, url, errorCallback) {
    const fontFace = new FontFace(family, url, {
        style: "normal",
        weight: "normal"
    });

    document.fonts.add(fontFace);

    fontFace.load()
        .then(result => errorCallback(""))
        .catch(error => errorCallback(error.name));
}

window.getMeta = function(name, defaultValue) {
    const meta = document.querySelector("meta[name='" + encodeURIComponent(name) + "']");
    return meta ? meta.content : defaultValue;
}

window.loadApplicationData = function(appName) {
    if (window.loadPreferences) {
        window.loadPreferences(appName);
    }
}

window.saveApplicationData = function(appName, name, value) {
    window.localStorage.setItem(name, value);

    if (window.savePreferences) {
        window.savePreferences(appName, name, value);
    }
}

window.requestTextInput = function(label, initialValue, callback) {
    const header = document.createElement("div");
    header.innerText = label;

    const input = document.createElement("input");
    input.type = "text";
    input.value = initialValue;

    const button = document.createElement("button");
    button.innerText = "OK";

    const inputForm = document.createElement("form");
    inputForm.appendChild(header);
    inputForm.appendChild(input);
    inputForm.appendChild(button);

    const container = document.getElementById("inputContainer");
    container.appendChild(inputForm);

    button.addEventListener("click", e => submitInputForm(e, inputForm, input.value, callback));
    inputForm.addEventListener("submit", e => submitInputForm(e, inputForm, input.value, callback));
    input.focus();
}

function submitInputForm(event, form, value, callback) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    if (value) {
        callback("inputForm", value);
        form.remove();
    }
    return false;
}
