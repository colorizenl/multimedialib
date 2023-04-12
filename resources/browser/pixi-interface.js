//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Interface for the parts of the Pixi.js interface implemented in JavaScript.
 * This interface is called from the renderer via TeaVM.
 */
class PixiInterface {

    constructor() {
        this.pixiApp = null;
        this.container = null;
        this.layers = {};
    }

    init() {
        this.pixiApp = new PIXI.Application({
            autoResize: true,
            backgroundColor: 0x000000
        });

        const canvasContainer = document.getElementById("multimediaLibContainer");
        canvasContainer.appendChild(this.pixiApp.view);

        this.container = new PIXI.Container();
        this.pixiApp.stage.addChild(this.container);

        this.layers = {};

        this.resize();
        window.addEventListener("resize", () => this.resize());
    }

    resize() {
        this.pixiApp.renderer.resize(window.innerWidth, window.innerHeight);
    }

    createLayer(layerName) {
        const layer = new PIXI.Container();
        this.layers[layerName] = layer;
        this.container.addChild(layer);
    }

    changeBackgroundColor(backgroundColor) {
        this.pixiApp.renderer.backgroundColor = backgroundColor;
    }

    addDisplayObject(layerName, displayObject) {
        this.layers[layerName].addChild(displayObject);
    }

    removeDisplayObject(layerName, displayObject) {
        if (this.layers[layerName]) {
            this.layers[layerName].removeChild(displayObject);
        }
    }

    clearStage() {
        Object.keys(this.layers).forEach(layerName => this.layers[layerName].removeChildren());
        this.container.removeChildren();
        this.layers = {};
    }

    createContainer() {
        return new PIXI.Container();
    }

    createSprite(image, regionX, regionY, regionWidth, regionHeight) {
        const baseTexture = new PIXI.BaseTexture(image);
        const region = new PIXI.Rectangle(regionX, regionY, regionWidth, regionHeight);
        const texture = new PIXI.Texture(baseTexture, region);

        const sprite = new PIXI.Sprite(texture);
        sprite.convertToHeaven();
        sprite.anchor.set(0.5);
        sprite.tintEnabled = false;
        return sprite;
    }

    createGraphics() {
        return new PIXI.Graphics();
    }

    createText(family, size, bold, align, lineHeight, color) {
        const textAligns = {left: 0.0, center: 0.5, right: 1.0};

        const richText = new PIXI.Text("", {
            fontFamily: family,
            fontSize: size,
            fontWeight: bold ? "bold" : "normal",
            fill: color,
            align: align.toLowerCase()
        });
        richText.anchor.set(textAligns[align.toLowerCase()], 0.0);
        return richText;
    }

    applyTint(sprite, tint) {
        sprite.tintEnabled = true;
        sprite.tint = tint;
        sprite.color.dark[0] = sprite.color.light[0];
        sprite.color.dark[1] = sprite.color.light[1];
        sprite.color.dark[2] = sprite.color.light[2];
        sprite.color.invalidate();
    }

    clearTint(sprite) {
        sprite.tintEnabled = false;
        sprite.tint = null;
        sprite.color.clear();
        sprite.color.invalidate();
    }

    createFilter(vertexGLSL, fragmentGLSL) {
        return new PIXI.Filter(vertexGLSL, fragmentGLSL, {});
    }

    applyFilter(layerName, filter) {
        this.layers[layerName].filters = filter ? [filter] : [];
    }
}
