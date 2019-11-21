//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.Alignment;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TrueTypeFont;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;

/**
 * Provides access to the renderer's drawing operations. The renderer only allows
 * drawing operations at the end of a frame updates, when the frame is rendered.
 * <p>
 * All drawing operations use X and Y coordinates that are relative to the canvas,
 * with the (0, 0) coordinate representing the top left corner. When drawing objects,
 * the X and Y coordinates indicate where the object's center will be drawn. There
 * is no explicit Z coordinate, depth ordering is based on the order in which the
 * graphics are drawn.
 */
public interface GraphicsContext {

    public Canvas getCanvas();

    default int getCanvasWidth() {
        return getCanvas().getWidth();
    }

    default int getCanvasHeight() {
        return getCanvas().getHeight();
    }

    public void drawBackground(ColorRGB backgroundColor);

    public void drawRect(Rect rect, ColorRGB color, Transform transform);

    public void drawPolygon(Polygon polygon, ColorRGB color, Transform transform);

    public void drawImage(Image image, float x, float y, Transform transform);

    default void drawSprite(Sprite sprite, float x, float y, Transform transform) {
        drawImage(sprite.getCurrentGraphics(), x, y, transform);
    }

    public void drawText(String text, TrueTypeFont font, float x, float y, Alignment align);

    default void drawText(String text, TrueTypeFont font, float x, float y) {
        drawText(text, font, x, y, Alignment.LEFT);
    }
}
