//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * TeaVM interface for the {@code pixi-interface.js} JavaScript implementation.
 */
public interface PixiInterface extends JSObject {

    @JSProperty
    public PixiDisplayObject getContainer();

    public void createLayer(String layerName);

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

    public PixiFilter createFilter(String vertexGLSL, String fragmentGLSL);

    public void applyFilter(String layerName, PixiFilter filter);
}
