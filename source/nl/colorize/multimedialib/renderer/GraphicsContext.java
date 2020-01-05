//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
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
 * drawing operations to be performed when the frame is rendered.
 * <p>
 * All drawing operations use X and Y coordinates that are relative to the canvas,
 * with the (0, 0) coordinate representing the top left corner. When drawing objects,
 * the X and Y coordinates indicate where the object's center will be drawn. There
 * is no explicit Z coordinate, depth ordering is based on the order in which the
 * graphics are drawn: graphics that are drawn later will overlap graphics that were
 * drawn earlier.
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

    default void drawRect(Rect rect, ColorRGB color) {
        drawRect(rect, color, null);
    }

    public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha);

    default void drawCircle(Circle circle, ColorRGB color) {
        drawCircle(circle, color, null);
    }

    public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha);

    default void drawPolygon(Polygon polygon, ColorRGB color) {
        drawPolygon(polygon, color, null);
    }

    public void drawImage(Image image, float x, float y, Transform transform);

    default void drawImage(Image image, float x, float y) {
        drawImage(image, x, y, null);
    }

    default void drawSprite(Sprite sprite, float x, float y, Transform transform) {
        drawImage(sprite.getCurrentGraphics(), x, y, transform);
    }

    default void drawSprite(Sprite sprite, float x, float y) {
        drawImage(sprite.getCurrentGraphics(), x, y, null);
    }

    public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha);

    default void drawText(String text, TTFont font, float x, float y, Align align) {
        drawText(text, font, x, y, align, null);
    }

    default void drawText(String text, TTFont font, float x, float y) {
        drawText(text, font, x, y, Align.LEFT, null);
    }
}
