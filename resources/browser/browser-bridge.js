//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Provides access to browser APIs that can be called from TeaVM. This bridge
 * is used for "general" browser APIs that do not depend on a specific renderer
 * but are always available.
 */
class BrowserBridge {

    isMobileDevice() {
        return /iphone|ipad|android/i.test(window.navigator.userAgent);
    }

    isTouchSupported() {
        return ("ontouchstart" in window) || window.navigator.maxTouchPoints > 0;
    }

    getQueryParameter(name, defaultValue) {
        const search = new URLSearchParams(window.location.search);
        return search.get(name) || defaultValue;
    }

    getMeta(name, defaultValue) {
        const meta = document.querySelector("meta[name='" + encodeURIComponent(name) + "']");
        return meta ? meta.content : defaultValue;
    }

    prepareAnimationLoop() {
        const spinner = document.getElementById("loading");
        spinner.style.display = "none";
    }

    registerErrorHandler(callback) {
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

    preloadFontFace(family, url, errorCallback) {
        const fontFace = new FontFace(family, url, {
            style: "normal",
            weight: "normal"
        });

        document.fonts.add(fontFace);

        fontFace.load()
            .then(result => errorCallback(""))
            .catch(error => errorCallback(error.name));
    }

    writeClipboard(text, callback) {
        setTimeout(() => {
            const data = new ClipboardItem({"text/plain": text});
            window.navigator.clipboard.write([data])
                .then(() => callback(""))
                .catch(() => callback("Clipboard is not available"));
        }, 1);
    }

    requestTextInput(label, initialValue, callback) {
        const header = document.createElement("div");
        header.innerText = label;

        const input = document.createElement("input");
        input.type = "text";
        input.value = initialValue;

        const button = document.createElement("button");
        button.innerText = "OK";

        const form = document.createElement("form");
        form.appendChild(header);
        form.appendChild(input);
        form.appendChild(button);

        const container = document.getElementById("inputContainer");
        container.appendChild(form);

        button.addEventListener("click", e => this.submitInputForm(e, form, input.value, callback));
        form.addEventListener("submit", e => this.submitInputForm(e, form, input.value, callback));
        input.focus();
    }

    submitInputForm(event, form, value, callback) {
        if (event.preventDefault) {
            event.preventDefault();
        }
        if (value) {
            callback("inputForm", value);
            form.remove();
        }
        return false;
    }

    loadApplicationData() {
        if (window.clrz) {
            window.clrz.loadPreferences();
        }
    }

    saveApplicationData(name, value) {
        window.localStorage.setItem(name, value);
        if (window.clrz) {
            window.clrz.savePreferences(name, value);
        }
    }
}
