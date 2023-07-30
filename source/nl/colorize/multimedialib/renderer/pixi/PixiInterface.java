//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.canvas.CanvasImageSource;

/**
 * TeaVM interface for the {@code pixi-interface.js} JavaScript implementation.
 */
public interface PixiInterface extends JSObject {

    public void init();

    @JSProperty
    public Pixi.App getPixiApp();

    @JSProperty
    public Pixi.DisplayObject getRootContainer();

    public void changeBackgroundColor(int backgroundColor);

    public Pixi.DisplayObject createContainer();

    public Pixi.Texture createTexture(String id, CanvasImageSource image,
                                      float regionX, float regionY,
                                      float regionWidth, float regionHeight);

    public Pixi.DisplayObject createSprite(Pixi.Texture texture);

    public Pixi.DisplayObject createGraphics();

    public Pixi.DisplayObject createText(String family, float size, boolean bold,
                                         String align, float lineHeight, int color);
}
