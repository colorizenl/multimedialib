//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

let canvas = null;
let imageContainer = null;
let images = {};
let imageDataCache = {};
let audioContainer = null;
let audio = {};
let fontContainer = null;
let resourceContainer = null;
let maskCache = {};

let pointerCoordinateRatio = 1;
let pointerEventBuffer = [];
let keyStates = {};

document.addEventListener("DOMContentLoaded", event => {
    registerServiceWorker();

    imageContainer = document.getElementById("imageContainer");
    audioContainer = document.getElementById("audioContainer");
    fontContainer = document.getElementById("fontContainer");
    resourceContainer = document.getElementById("resourceContainer");

    const container = document.getElementById("multimediaLibContainer");
    initCanvas(container);
    main();
});

function initCanvas(container) {
    const width = Math.round(container.offsetWidth);
    const height = Math.round(document.documentElement.clientHeight);

    canvas = createCanvas(width, height);
    container.appendChild(canvas);
    initEventHandlers(container);

    document.getElementById("loading").style.display = "none";
}

function initEventHandlers(container) {
    canvas.addEventListener("mousedown", event => registerMouseEvent("mousedown", event), false);
    canvas.addEventListener("mouseup", event => registerMouseEvent("mouseup", event), false);
    canvas.addEventListener("mousemove", event => registerMouseEvent("mousemove", event), false);
    window.addEventListener("mouseout", event => registerMouseEvent("mouseout", event), false);
    canvas.addEventListener("touchstart", event => registerTouchEvent("touchstart", event), false);
    canvas.addEventListener("touchend", event => registerTouchEvent("touchend", event), false);
    canvas.addEventListener("touchmove", event => registerTouchEvent("touchmove", event), false);
    window.addEventListener("touchcancel", event => registerTouchEvent("touchcancel", event), false);
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
    window.addEventListener("resize", () => resizeCanvas(container));
}

function createCanvas(width, height) {
    const newCanvas = document.createElement("canvas");
    newCanvas.style.width = width + "px";
    newCanvas.style.height = height + "px";
    newCanvas.width = width * window.devicePixelRatio;
    newCanvas.height = height * window.devicePixelRatio;
    return newCanvas;
}

function getRequestedRenderer() {
    const urlParam = window.location.href.match(/renderer=(\w+)/);
    if (urlParam != null) {
        return urlParam[1];
    }

    const metaTags = document.getElementsByTagName("meta");
    for (let i = 0; i < metaTags.length; i++) {
        if (metaTags[i].getAttribute("name") == "renderer") {
            return metaTags[i].getAttribute("content");
        }
    }
    
    throw "Renderer not specified";
}

function resizeCanvas(container) {
    const targetWidth = Math.round(container.offsetWidth);
    const targetHeight = Math.round(document.documentElement.clientHeight);

    canvas.style.width = targetWidth + "px";
    canvas.style.height = targetHeight + "px";
    canvas.width = targetWidth * window.devicePixelRatio;
    canvas.height = targetHeight * window.devicePixelRatio;
}

function registerMouseEvent(eventType, event) {
    const mouseX = event.pageX / pointerCoordinateRatio - canvas.offsetLeft;
    const mouseY = event.pageY / pointerCoordinateRatio - canvas.offsetTop;
    registerPointerEvent(eventType + ";mouse;" + mouseX + ";" + mouseY);
    cancelEvent(event);
}

function registerTouchEvent(eventType, event) {
    for (let i = 0; i < event.changedTouches.length; i++) {
        let identifier = event.changedTouches[i].identifier;
        let touchX = event.changedTouches[i].pageX - canvas.offsetLeft;
        let touchY = event.changedTouches[i].pageY - canvas.offsetTop;
        registerPointerEvent(eventType + ";" + identifier + ";" + touchX + ";" + touchY);
    }
}

function registerPointerEvent(entry) {
    pointerEventBuffer.push(entry);
}

function flushPointerEventBuffer() {
    let flushed = pointerEventBuffer;
    pointerEventBuffer = [];
    return flushed;
}

function onKeyDown(event) {
    keyStates[event.keyCode] = 1;
    cancelEvent(event);
}

function onKeyUp(event) {
    keyStates[event.keyCode] = 0;
    cancelEvent(event);
}

function cancelEvent(event) {
    event.preventDefault();
    event.stopPropagation();
}

function loadImage(id, path) {
    let imageElement = document.createElement("img");
    imageElement.src = path;
    imageContainer.appendChild(imageElement);
    images[id] = imageElement;
}

function tintImage(originalId, newId, color) {
    let image = images[originalId];
    let tinted = createCanvas(image.width, image.height);
    let tintedContext = tinted.getContext("2d");
    tintedContext.drawImage(image, 0, 0, image.width, image.height);
    tintedContext.globalCompositeOperation = "source-atop";
    tintedContext.fillStyle = color;
    tintedContext.fillRect(0, 0, image.width, image.height);

    images[newId] = tinted;
    return tinted;
}

function prepareImage(imageId, mask) {
    const image = images[imageId];

    if (!mask) {
        return image;
    }

    const cacheKey = image.width + "x" + image.height;
    let maskImageCanvas = maskCache[cacheKey];

    if (maskImageCanvas == null) {
        maskImageCanvas = createCanvas(image.width, image.height);
        maskCache[cacheKey] = maskImageCanvas;
    }

    const maskImageContext = maskImageCanvas.getContext("2d");
    maskImageContext.drawImage(image, 0, 0, image.width, image.height);
    maskImageContext.globalCompositeOperation = "source-atop";
    maskImageContext.fillStyle = mask;
    maskImageContext.fillRect(0, 0, image.width, image.height);

    return maskImageCanvas;
}

function loadAudio(id, path) {
    audio[id] = new Audio(path);
}

function loadFont(id, path, fontFamily) {
    let css = "";
    css += "@font-face { ";
    css += "    font-family: '" + fontFamily + "'; ";
    css += "    font-style: normal; ";
    css += "    font-weight: 400; ";
    css += "    src: url('" + path + "') format('truetype'); ";
    css += "}; ";

    const style = document.createElement("style");
    style.type = "text/css";
    style.appendChild(document.createTextNode(css));
    fontContainer.appendChild(style);
}

function loadTextFile(id) {
	let resource = document.getElementById(id);
	if (resource == null) {
		return null;
	}
	return resource.innerHTML;
}

function getImageData(id, x, y) {
    if (images[id]) {
    	let image = images[id];
        let imageData = imageDataCache[id];

        if (imageData == null) {
            let imageCanvas = createCanvas(image.width, image.height);
            let imageCanvasContext = imageCanvas.getContext("2d");
            imageCanvasContext.drawImage(image, 0, 0);
            imageData = imageCanvasContext;

            if (isImageDataAvailable(imageData, x, y)) {
                imageDataCache[id] = imageData;
            }
        }

        return imageData.getImageData(x, y, 1, 1).data;
    } else {
        return [-1, -1, -1, 255];
    }
}

function isImageDataAvailable(imageData, x, y) {
    let rgba = imageData.getImageData(x, y, 1, 1).data;
    return (rgba[0] + rgba[1] + rgba[2] + rgba[3]) != 0;
}

function playAudio(id, volume, loop) {
    audio[id].volume = volume;
    audio[id].loop = loop;
    audio[id].play();
}

function stopAudio(id, reset) {
    audio[id].pause();
    if (reset) {
        audio[id].currentTime = 0.0;
    }
}

function registerServiceWorker() {
    const browserSupport = "serviceWorker" in navigator;
    const hasManifest = document.querySelectorAll("link[rel=manifest]").length > 0;
    const isLocal = window.location.protocol.indexOf("file") != -1;

    if (browserSupport && hasManifest && !isLocal) {
        navigator.serviceWorker.register("service-worker.js")
            .then(() => console.log("Registered service worker"))
            .catch(e => console.log("Failed to register service worker"));
    } else {
        console.log("Service worker not supported");
    }
}
