//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

let canvas = null;
let overlayCanvas = null;
let renderer = null;
let lastFrameTime = null;

let imageContainer = null;
let images = {};
let imageDataURLs = {};
let imageDataCache = {};
let audioContainer = null;
let audio = {};
let fontContainer = null;
let resourceContainer = null;

let pointerEventBuffer = [];
let keyStates = {};

const MIN_FRAME_TIME = 10;
const MAX_FRAME_TIME = 40;
const IMAGE_POLL_INTERVAL = 500;

document.addEventListener("DOMContentLoaded", event => {
    imageContainer = document.getElementById("imageContainer");
    audioContainer = document.getElementById("audioContainer");
    fontContainer = document.getElementById("fontContainer");
    resourceContainer = document.getElementById("resourceContainer");

    let container = document.getElementById("multimediaLibContainer");
    initCanvas(container);
    main();
});

function initCanvas(container) {
    let width = Math.round(container.offsetWidth);
    let height = Math.round(document.documentElement.clientHeight);

    renderer = createRenderer(container, width, height);
    canvas = renderer.getCanvas();
    initEventHandlers(container);

    if (renderer.hasOverlayCanvas()) {
        overlayCanvas = createCanvas(width, height);
        overlayCanvas.id = "overlayCanvas";
        container.appendChild(overlayCanvas);
    }

    console.log("Starting renderer " + renderer.getName());
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
    let newCanvas = document.createElement("canvas");
    newCanvas.style.width = width + "px";
    newCanvas.style.height = height + "px";
    newCanvas.width = width * window.devicePixelRatio;
    newCanvas.height = height * window.devicePixelRatio;
    return newCanvas;
}

function createRenderer(container, width, height) {
    let requestedRenderer = getRequestedRenderer();
    
    if (requestedRenderer == "three") {
        if (checkWebGL()) {
            return new ThreejsRenderer(container);
        } else {
            container.innerHTML = "Your browser does not support WebGL.";
        }
    } else if (requestedRenderer == "webgl" && checkWebGL()) {
        canvas = createCanvas(width, height);
        container.appendChild(canvas);
        return new WebGL2DRenderer(canvas.getContext("webgl"));
    } else {
        canvas = createCanvas(width, height);
        container.appendChild(canvas);
        return new Html5CanvasRenderer(canvas.getContext("2d"));
    }
}

function getRequestedRenderer() {
    let requested = window.location.href.match(/renderer=(\w+)/);
    if (requested != null) {
        return requested[1];
    }

    let metaTags = document.getElementsByTagName("meta");
    for (let i = 0; i < metaTags.length; i++) {
        if (metaTags[i].getAttribute("name") == "renderer") {
            return metaTags[i].getAttribute("content");
        }
    }
    
    throw "Renderer not specified";
}

function checkWebGL() {
    let canvas = document.createElement("canvas");
    let context = canvas.getContext("webgl");
    return context != null && context instanceof WebGLRenderingContext;
}

function resizeCanvas(container) {
    let targetWidth = Math.round(container.offsetWidth);
    let targetHeight = Math.round(document.documentElement.clientHeight);

    canvas.style.width = targetWidth + "px";
    canvas.style.height = targetHeight + "px";
    canvas.width = targetWidth * window.devicePixelRatio;
    canvas.height = targetHeight * window.devicePixelRatio;

    if (overlayCanvas != null) {
        overlayCanvas.style.width = targetWidth + "px";
        overlayCanvas.style.height = targetHeight + "px";
        overlayCanvas.width = targetWidth * window.devicePixelRatio;
        overlayCanvas.height = targetHeight * window.devicePixelRatio;
    }
}

/**
 * Starts the animation loop. This method is called from TeaVM once the
 * application has been initialized. It will keep calling onFrame() to
 * perform frame updates.
 */
function startAnimationLoop(callback) {
    onFrame(null, callback, true);
}

/**
 * Callback method that is called every frame during the animation loop.
 * This will in turn call the callback method provided through TeaVM to
 * perform updates and rendering.
 */
function onFrame(time, callback, render) {
    let deltaTime = Math.round(time - (lastFrameTime || time));
    deltaTime = Math.max(deltaTime, MIN_FRAME_TIME);
    deltaTime = Math.min(deltaTime, MAX_FRAME_TIME);

    lastFrameTime = time;

    renderer.render(deltaTime / 1000.0);
    callback(deltaTime / 1000.0, render);

    window.requestAnimationFrame(nextTime => onFrame(nextTime, callback, true));
}

function registerMouseEvent(eventType, event) {
    let mouseX = event.pageX - canvas.offsetLeft;
    let mouseY = event.pageY - canvas.offsetTop;
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
    imageElement.onload = () => renderer.onLoadImage(id, imageElement);
    imageContainer.appendChild(imageElement);
    images[id] = imageElement;

    // The image is loaded as a data URL, which is in turn loaded
    // using an AJAX request. This ensures that image data can be
    // read even when running the application locally.
    pollImageLoad(imageElement, path);
}

function pollImageLoad(imageElement, path) {
    if (imageDataURLs[path] != null) {
        imageElement.src = imageDataURLs[path];
    } else {
        setTimeout(() => pollImageLoad(imageElement, path), IMAGE_POLL_INTERVAL);
    }
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

    let style = document.createElement("style");
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

/**
 * Converts a hexadecimal color to an array of red, green, and blue components
 * represented by an integer between 0 and 255. For example, #FF0000 will return
 * the array [255, 0, 0].
 */
function toRGB(hexColor) {
    if (hexColor.length != 7) {
        throw "Invalid hexadecimal color: " + hexColor;
    }

    return [
        parseInt(hexColor.substring(1, 3), 16),
        parseInt(hexColor.substring(3, 5), 16),
        parseInt(hexColor.substring(5, 7), 16)
    ];
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

function takeScreenshot() {
    if (canvas == null) {
        throw "Canvas not yet initialized";
    }
    return canvas.toDataURL();
}
