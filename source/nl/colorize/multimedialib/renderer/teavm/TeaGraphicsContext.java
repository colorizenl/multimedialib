//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext;

/**
 * Delegates all drawing operations to the browser using TeaVM. The actual
 * implementations of the drawing operations can be found in JavaScript.
 */
public class TeaGraphicsContext implements GraphicsContext {

    private Canvas canvas;

    private static final Transform NULL_TRANSFORM = new Transform();

    public TeaGraphicsContext(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), backgroundColor, null);
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, AlphaTransform alpha) {
        float x = canvas.toScreenX(rect.getX());
        float y = canvas.toScreenX(rect.getY());
        float width = canvas.toScreenX(rect.getWidth());
        float height = canvas.toScreenX(rect.getHeight());

        Browser.drawRect(x, y, width, height, color.toHex(), getAlphaValue(alpha));
    }

    @Override
    public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha) {
        float x = canvas.toScreenX(circle.getCenterX());
        float y = canvas.toScreenY(circle.getCenterY());
        float radius = circle.getRadius() * canvas.getZoomLevel();

        Browser.drawCircle(x, y, radius, color.toHex(), getAlphaValue(alpha));
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha) {
        float[] points = new float[polygon.getPoints().length];

        for (int i = 0; i < polygon.getPoints().length; i += 2) {
            points[i] = canvas.toScreenX(polygon.getPoints()[i]);
            points[i + 1] = canvas.toScreenY(polygon.getPoints()[i + 1]);
        }

        Browser.drawPolygon(points, color.toHex(), getAlphaValue(alpha));
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
        if (transform == null) {
            transform = NULL_TRANSFORM;
        }

        TeaImage pointer = (TeaImage) image;
        float canvasX = canvas.toScreenX(x);
        float canvasY = canvas.toScreenY(y);

        if (pointer.getRegion() == null) {
            float width = image.getWidth() * canvas.getZoomLevel();
            float height = image.getHeight() * canvas.getZoomLevel();

            Browser.drawImage(pointer.getId(), canvasX - width / 2f, canvasY - height / 2f,
                width, height, transform.getAlpha() / 100f, getMask(transform));
        } else {
            Rect region = pointer.getRegion();
            float width = region.getWidth() * canvas.getZoomLevel();
            float height = region.getHeight() * canvas.getZoomLevel();

            Browser.drawImageRegion(pointer.getId(), region.getX(), region.getY(),
                region.getWidth(), region.getHeight(),
                canvasX, canvasY, width, height, transform.getAlpha() / 100f, getMask(transform));
        }
    }

    @Override
    public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha) {
        float canvasX = canvas.toScreenX(x);
        float canvasY = canvas.toScreenY(y);

        Browser.drawText(text, font.getFamily(), font.getSize(), font.getColor().toHex(),
            canvasX, canvasY, align.toString().toLowerCase(), getAlphaValue(alpha));
    }

    private float getAlphaValue(AlphaTransform alpha) {
        if (alpha == null) {
            return 1f;
        }
        return alpha.getAlpha() / 100f;
    }

    private String getMask(Transform transform) {
        if (transform == null || transform.getMask() == null) {
            return null;
        }
        return transform.getMask().toHex();
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }
}
