//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;

import java.util.List;

/**
 * Provides access to the renderer's 2D drawing operations. The renderer only
 * allows drawing operations to be performed when the frame is rendered.
 * <p>
 * All drawing operations use X and Y coordinates that are relative to the
 * canvas, with the (0, 0) coordinate representing the top left corner. When
 * drawing objects, the X and Y coordinates indicate where the object's center
 * will be drawn. There is no explicit Z coordinate, depth ordering is based on
 * the order in which the graphics are drawn: graphics that are drawn later will
 * overlap graphics that were drawn earlier.
 */
public interface GraphicsContext2D {

    public Canvas getCanvas();

    default int getCanvasWidth() {
        return getCanvas().getWidth();
    }

    default int getCanvasHeight() {
        return getCanvas().getHeight();
    }

    public void drawBackground(ColorRGB backgroundColor);

    /**
     * Draws a line between the specified two points.
     *
     * @deprecated Drawing lines is not supported by all renderers. Moreover,
     *             some renderers do support drawing lines, but the operation
     *             is not hardware-accelerated, meaning the performance behavior
     *             cannot be guaranteed. Therefore, only use this method for
     *             testing and debugging purposes.
     */
    @Deprecated
    public void drawLine(Point2D from, Point2D to, ColorRGB color, float thickness);

    public void drawRect(Rect rect, ColorRGB color, AlphaTransform alpha);

    default void drawRect(Rect rect, ColorRGB color, float alpha) {
        drawRect(rect, color, () -> alpha);
    }

    default void drawRect(Rect rect, ColorRGB color) {
        drawRect(rect, color, null);
    }

    public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha);

    default void drawCircle(Circle circle, ColorRGB color, float alpha) {
        drawCircle(circle, color, () -> alpha);
    }

    default void drawCircle(Circle circle, ColorRGB color) {
        drawCircle(circle, color, null);
    }

    public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha);

    default void drawPolygon(Polygon polygon, ColorRGB color, float alpha) {
        drawPolygon(polygon, color, () -> alpha);
    }

    default void drawPolygon(Polygon polygon, ColorRGB color) {
        drawPolygon(polygon, color, null);
    }

    public void drawImage(Image image, float x, float y, Transform transform);

    default void drawImage(Image image, float x, float y) {
        drawImage(image, x, y, null);
    }

    default void drawSprite(Sprite sprite) {
        drawImage(sprite.getCurrentGraphics(), sprite.getPosition().getX(),
            sprite.getPosition().getY(), sprite.getTransform());
    }

    /**
     * Draws an image and stretches it so that it fills the entire canvas.
     * Note that depending on the canvas size this might mean that the
     * image will be drawn at a different aspect ratio.
     */
    default void fillImage(Image image) {
        float scaleX = (float) getCanvasWidth() / (float) image.getWidth() * 100f;
        float scaleY = (float) getCanvasHeight() / (float) image.getHeight() * 100f;
        Transform transform = Transform.withScale(scaleX, scaleY);
        drawImage(image, getCanvasWidth() / 2f, getCanvasHeight() / 2f, transform);
    }

    public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha);

    default void drawText(String text, TTFont font, float x, float y, Align align) {
        drawText(text, font, x, y, align, null);
    }

    default void drawText(String text, TTFont font, float x, float y) {
        drawText(text, font, x, y, Align.LEFT, null);
    }

    /**
     * Draws a text block that spans multiple lines, based on the occurrence of
     * line breaks ({@code \n}) in the text. The line height is based on
     * {@link TTFont#getLineHeight()}. This method is provided separately from
     * the "normal" {@code drawText} because many platforms do not support
     * multi-line text natively.
     */
    default void drawMultiLineText(String text, TTFont font, float x, float y, Align align) {
        Splitter lineSplitter = Splitter.on("\n").trimResults();
        List<String> lines = lineSplitter.splitToList(text);

        for (int i = 0; i < lines.size(); i++) {
            drawText(lines.get(i), font, x, y + i * font.getLineHeight(), align);
        }
    }
}
