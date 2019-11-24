//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Circle;
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

    public void drawRect(Rect rect, ColorRGB color, AlphaTransform alpha);

    public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha);

    public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha);

    public void drawImage(Image image, float x, float y, Transform transform);

    default void drawSprite(Sprite sprite, float x, float y, Transform transform) {
        drawImage(sprite.getCurrentGraphics(), x, y, transform);
    }

    public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha);

    default void drawText(String text, TTFont font, float x, float y, Align align) {
        drawText(text, font, x, y, align, null);
    }

    default void drawText(String text, TTFont font, float x, float y) {
        drawText(text, font, x, y, Align.LEFT, null);
    }
}
