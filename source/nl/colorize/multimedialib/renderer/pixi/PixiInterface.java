//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.html.HTMLImageElement;

/**
 * TeaVM interface for the {@code pixi-interface.js} JavaScript implementation.
 */
public interface PixiInterface extends JSObject {

    public void init();

    @JSProperty
    public Pixi.App getPixiApp();

    @JSProperty
    public Pixi.DisplayObject getContainer();

    public void createLayer(String layerName);

    public void changeBackgroundColor(int backgroundColor);

    public void addDisplayObject(String layerName, Pixi.DisplayObject displayObject);

    public void removeDisplayObject(String layerName, Pixi.DisplayObject displayObject);

    public void clearStage();

    public Pixi.DisplayObject createContainer();

    public Pixi.DisplayObject createSprite(HTMLImageElement image, float regionX, float regionY,
                                           float regionWidth, float regionHeight);

    public Pixi.DisplayObject createGraphics();

    public Pixi.DisplayObject createText(String family, float size, boolean bold,
                                         String align, float lineHeight, int color);

    public void applyTint(Pixi.DisplayObject sprite, int tintColor);

    public void clearTint(Pixi.DisplayObject sprite);

    public Pixi.Filter createFilter(String vertexGLSL, String fragmentGLSL);

    public void applyFilter(String layerName, Pixi.Filter filter);
}
