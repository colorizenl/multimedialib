//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

var canvas = null;
var context = null;

var imageContainer = null;
var images = {};
var audioContainer = null;
var audio = {};
var fontContainer = null;
var resourceContainer = null;

var pointerX = 0;
var pointerY = 0;
var pointerState = 0;
var keyStates = {};

var maskCache = {};

var ASPECT_RATIO = 1.6;

document.addEventListener("DOMContentLoaded", function(event) {
    var container = document.getElementById("multimediaLibContainer");
    initContainer(container);
    initCanvas(container);
    main();
});

function initContainer(container) {
    document.body.style.margin = "0px";
    document.body.style.backgroundColor = "#343434";

    imageContainer = document.getElementById("imageContainer");
    imageContainer.style.display = "none";

    audioContainer = document.getElementById("audioContainer");
    audioContainer.style.display = "none";

    fontContainer = document.getElementById("fontContainer");
    fontContainer.style.display = "none";

    resourceContainer = document.getElementById("resourceContainer");
    resourceContainer.style.display = "none";
}

function initCanvas(container) {
    var width = Math.round(container.offsetWidth);
    var height = Math.round(width / ASPECT_RATIO);

    canvas = createCanvas(width, height);
    canvas.style["margin-left"] = "auto";
    canvas.style["margin-right"] = "auto";
    canvas.addEventListener("mousemove", onMouseMove);
    canvas.addEventListener("mousedown", onMouseDown);
    canvas.addEventListener("mouseup", onMouseUp);
    canvas.addEventListener("mouseout", onMouseUp);
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
    container.appendChild(canvas);

    context = canvas.getContext("2d");

    document.getElementById("loading").style.display = "none";
}

function createCanvas(width, height) {
    var c = document.createElement("canvas");
    c.width = width;
    c.height = height;
    return c;
}

function onFrame(callback) {
    context.clearRect(0, 0, canvas.width, canvas.height);

    callback();

    window.requestAnimationFrame(function() {
        onFrame(callback);
    });
}

function drawRect(x, y, width, height, color, alpha) {
    context.globalAlpha = alpha;
    context.fillStyle = color;
    context.fillRect(x, y, width, height);
    context.globalAlpha = 1.0;
}

function drawPolygon(points, color, alpha) {
    context.fillStyle = color;
    context.globalAlpha = alpha;
    context.beginPath();
    context.moveTo(points[0], points[1]);
    for (var i = 2; i < points.length; i += 2) {
        context.lineTo(points[i], points[i + 1]);
    }
    context.fill();
    context.globalAlpha = 1.0;
}

function drawImage(id, x, y, width, height, alpha, mask) {
    if (images[id]) {
        var image = prepareImage(images[id], mask);

        context.globalAlpha = alpha;
        context.drawImage(image, x, y, width, height);
        context.globalAlpha = 1.0;
    }
}

function drawImageRegion(id, regionX, regionY, regionWidth, regionHeight, x, y, width, height,
                         alpha, mask) {
    if (images[id]) {
        var image = prepareImage(images[id], mask);

        context.globalAlpha = alpha;
        context.drawImage(image, regionX, regionY, regionWidth, regionHeight,
                          x, y, width, height);
        context.globalAlpha = 1.0;
    }
}

function prepareImage(image, mask) {
    if (!mask) {
        return image;
    }

    var cacheKey = image.width + "x" + image.height;
    var maskImageCanvas = maskCache[cacheKey];

    if (maskImageCanvas == null) {
        maskImageCanvas = createCanvas(image.width, image.height);
        maskCache[cacheKey] = maskImageCanvas;
    }

    var maskImageContext = maskImageCanvas.getContext("2d");
    maskImageContext.drawImage(image, 0, 0, image.width, image.height);
    maskImageContext.globalCompositeOperation = "source-atop";
    maskImageContext.fillStyle = mask;
    maskImageContext.fillRect(0, 0, image.width, image.height);

    return maskImageCanvas;
}

function drawText(text, font, size, color, x, y, align) {
    context.fillStyle = color;
    context.font = size + "px " + font;
    context.textAlign = align;
    context.fillText(text, x, y);
}

function onMouseMove(event) {
    var bounds = canvas.getBoundingClientRect();
    pointerX = event.clientX - bounds.left;
    pointerY = event.clientY - bounds.top;
}

function onMouseDown(event) {
    pointerState = 1;
}

function onMouseUp(event) {
    pointerState = 0;
}

function onKeyDown(event) {
    keyStates[event.keyCode] = 1;
}

function onKeyUp(event) {
    keyStates[event.keyCode] = 0;
}

function loadImage(id, path) {
    var imageElement = document.createElement("img");
    imageElement.src = path;
    imageContainer.appendChild(imageElement);
    images[id] = imageElement;
}

function loadFont(id, path, fontFamily) {
    var css = "";
    css += "@font-face { ";
    css += "    font-family: '" + fontFamily + "'; ";
    css += "    font-style: normal; ";
    css += "    font-weight: 400; ";
    css += "    src: url('" + path + "') format('truetype'); ";
    css += "}; ";

    var style = document.createElement('style');
    style.type = 'text/css';
    style.appendChild(document.createTextNode(css));
    fontContainer.appendChild(style);
}

function sendGetRequest(url) {
    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.send();
}

function sendPostRequest(url, params) {
    var request = new XMLHttpRequest();
    request.open("POST", url, true);
    request.send(params);
}
