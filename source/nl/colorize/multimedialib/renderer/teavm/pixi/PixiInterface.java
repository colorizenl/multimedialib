//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm.pixi;

import org.teavm.jso.JSObject;

/**
 * TeaVM interface for the {@code Pixi.js} renderer functionality implemented
 * in JavaScript.
 */
public interface PixiInterface extends JSObject {

    public void createLayer(String layerName);

    public boolean hasLayer(String layerName);

    public void changeBackgroundColor(int backgroundColor);

    public PixiDisplayObject createSprite(String layerName, String imageId,
                                              float regionX, float regionY,
                                              float regionWidth, float regionHeight);

    public PixiDisplayObject createGraphics(String layerName);

    public PixiDisplayObject createText(String layerName, String family, float size, boolean bold,
                                        String align, float lineHeight, int color);

    public void removeDisplayObject(String layerName, PixiDisplayObject displayObject);

    public void clearStage();

    public void applyTint(PixiDisplayObject sprite, int tintColor);

    public void clearTint(PixiDisplayObject sprite);
}
