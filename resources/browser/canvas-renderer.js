//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Renderer implementation that uses the HTML5 canvas drawing API. Whether drawing
 * operations are hardware accelerated depends on the browser and on the platform.
 */
class Html5CanvasRenderer {

    constructor(context) {
        this.context = context;
        this.maskCache = {};
    }

    render(deltaTime) {
        this.context.clearRect(0, 0, canvas.width, canvas.height);
    }

    getCanvas() {
        return canvas;
    }

    hasOverlayCanvas() {
        return false;
    }

    onLoadImage(id, imageElement) {
    }

    drawLine(x0, y0, x1, y1, color, thickness) {
        this.context.globalAlpha = 1.0;
        this.context.fillStyle = color;
        this.context.lineWidth = thickness || 1.0;
        this.context.beginPath();
        this.context.moveTo(0, 0);
        this.context.lineTo(300, 150);
        this.context.stroke();
    }

    drawRect(x, y, width, height, color, alpha) {
        this.context.globalAlpha = alpha;
        this.context.fillStyle = color;
        this.context.fillRect(x, y, width, height);
        this.context.globalAlpha = 1.0;
    }

    drawCircle(x, y, radius, color, alpha) {
        this.context.globalAlpha = alpha;
        this.context.fillStyle = color;
        this.context.beginPath();
        this.context.arc(x, y, radius, 0, 2.0 * Math.PI);
        this.context.fill();
        this.context.globalAlpha = 1.0;
    }

    drawPolygon(points, color, alpha) {
        this.context.fillStyle = color;
        this.context.globalAlpha = alpha;
        this.context.beginPath();
        this.context.moveTo(points[0], points[1]);
        for (let i = 2; i < points.length; i += 2) {
            this.context.lineTo(points[i], points[i + 1]);
        }
        this.context.fill();
        this.context.globalAlpha = 1.0;
    }

    drawImage(id, x, y, width, height, alpha, mask) {
        if (this.isImageAvailable(id)) {
            this.drawImageRegion(id, 0, 0, images[id].width, images[id].height, x, y,
                width, height, 0.0, 1.0, 1.0, alpha, mask);
        }
    }

    drawImageRegion(id, regionX, regionY, regionWidth, regionHeight, x, y, width, height,
                    rotation, scaleX, scaleY, alpha, mask) {
        if (this.isImageAvailable(id)) {
            let image = this.prepareImage(images[id], mask);

            this.context.globalAlpha = alpha;
            this.context.translate(x, y);
            this.context.rotate(rotation);
            this.context.scale(scaleX, scaleY);
            this.context.drawImage(image, regionX, regionY, regionWidth, regionHeight,
                -width / 2.0, -height / 2.0, width, height);

            this.context.globalAlpha = 1.0;
            this.context.resetTransform();
        }
    }
    
    isImageAvailable(id) {
        return images[id] && images[id].width > 0 && images[id].height > 0;
    }

    prepareImage(image, mask) {
        if (!mask) {
            return image;
        }

        let cacheKey = image.width + "x" + image.height;
        let maskImageCanvas = this.maskCache[cacheKey];

        if (maskImageCanvas == null) {
            maskImageCanvas = createCanvas(image.width, image.height);
            this.maskCache[cacheKey] = maskImageCanvas;
        }

        let maskImageContext = maskImageCanvas.getContext("2d");
        maskImageContext.drawImage(image, 0, 0, image.width, image.height);
        maskImageContext.globalCompositeOperation = "source-atop";
        maskImageContext.fillStyle = mask;
        maskImageContext.fillRect(0, 0, image.width, image.height);

        return maskImageCanvas;
    }

    drawText(text, font, size, color, bold, x, y, align, alpha) {
        this.context.globalAlpha = alpha;
        this.context.fillStyle = color;
        this.context.font = (bold ? "bold " : "") + size + "px " + font;
        this.context.textAlign = align;
        this.context.fillText(text, x, y);
        this.context.globalAlpha = 1.0;
    }
    
    getName() {
        return "HTML5 canvas renderer";
    }
}
