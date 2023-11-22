//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Bridge interface for the parts of the Pixi.js interface implemented in
 * JavaScript. This interface is called from the renderer via TeaVM.
 */
class PixiBridge {

    constructor() {
        this.pixiApp = null;
        this.rootContainer = null;
    }

    init() {
        this.pixiApp = new PIXI.Application({
            autoResize: true,
            backgroundColor: 0x000000
        });

        const canvasContainer = document.getElementById("multimediaLibContainer");
        canvasContainer.appendChild(this.pixiApp.view);

        this.rootContainer = new PIXI.Container();
        this.pixiApp.stage.addChild(this.rootContainer);

        this.resize();
        window.addEventListener("resize", () => this.resize());
    }

    resize() {
        this.pixiApp.renderer.resize(window.innerWidth, window.innerHeight);
    }

    changeBackgroundColor(backgroundColor) {
        this.pixiApp.renderer.background.color = backgroundColor;
    }

    createContainer() {
        return new PIXI.Container();
    }

    createTexture(id, image, regionX, regionY, regionWidth, regionHeight) {
        const baseTexture = new PIXI.BaseTexture(image);
        const region = new PIXI.Rectangle(regionX, regionY, regionWidth, regionHeight);

        const texture = new PIXI.Texture(baseTexture, region);
        texture.textureImageId = id;
        return texture;
    }

    createSprite(texture) {
        const sprite = new PIXI.Sprite(texture);
        sprite.anchor.set(0.5);
        sprite.originalTexture = texture;
        sprite.tintEnabled = false;
        return sprite;
    }

    createGraphics() {
        return new PIXI.Graphics();
    }

    createText(family, size, bold, align, lineHeight, color) {
        const textAligns = {
            left: 0.0,
            center: 0.5,
            right: 1.0
        };

        const richText = new PIXI.Text("", {
            fontFamily: family,
            fontSize: size,
            fontWeight: bold ? "bold" : "normal",
            fill: color,
            align: align.toLowerCase(),
            lineHeight: lineHeight
        });
        richText.anchor.set(textAligns[align.toLowerCase()], 0.0);
        return richText;
    }
}
