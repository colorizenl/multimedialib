//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.util.swing.Utils2D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses Java 2D to render graphics. Because of Java 2D's flexibility this class
 * supports several graphics contexts: drawing can be either directly to a
 * window using active rendering, but also to a Swing component, or to an image.
 */
public class Java2DGraphicsContext implements GraphicsContext2D {

    private Canvas canvas;
    private Graphics2D g2;
    private StandardMediaLoader mediaLoader;

    private Map<ColorRGB, Color> colorCache;
    private Map<String, BufferedImage> maskCache;

    private static final Transform NULL_TRANSFORM = new Transform();

    public Java2DGraphicsContext(Canvas canvas, StandardMediaLoader mediaLoader) {
        this.canvas = canvas;
        this.mediaLoader = mediaLoader;

        this.colorCache = new HashMap<>();
        this.maskCache = new HashMap<>();
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    public void bind(Graphics2D g2) {
        this.g2 = g2;
    }

    public void dispose() {
        if (this.g2 != null) {
            g2.dispose();
            g2 = null;
        }
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        float width = canvas.toScreenX(canvas.getWidth());
        float height = canvas.toScreenY(canvas.getHeight());

        g2.setColor(convertColor(backgroundColor));
        g2.fillRect(0, 0, Math.round(width), Math.round(height) + 30);
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, AlphaTransform alpha) {
        float screenX = canvas.toScreenX(rect.getX());
        float screenY = canvas.toScreenY(rect.getY());
        float screenWidth = canvas.toScreenX(rect.getEndX()) - screenX;
        float screenHeight = canvas.toScreenY(rect.getEndY()) - screenY;

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(alpha);
        g2.setColor(convertColor(color));
        g2.fillRect(Math.round(screenX), Math.round(screenY), Math.round(screenWidth),
            Math.round(screenHeight));
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawCircle(Circle circle, ColorRGB color, AlphaTransform alpha) {
        float screenX = canvas.toScreenX(circle.getCenterX() - circle.getRadius());
        float screenY = canvas.toScreenY(circle.getCenterY() - circle.getRadius());
        float screenWidth = canvas.toScreenX(circle.getCenterX() + circle.getRadius()) - screenX;
        float screenHeight = canvas.toScreenY(circle.getCenterY() + circle.getRadius()) - screenY;

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(alpha);
        g2.setColor(convertColor(color));
        g2.fillOval(Math.round(screenX), Math.round(screenY), Math.round(screenWidth),
            Math.round(screenHeight));
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, AlphaTransform alpha) {
        int[] px = new int[polygon.getNumPoints()];
        int[] py = new int[polygon.getNumPoints()];

        for (int i = 0; i < polygon.getNumPoints(); i++) {
            px[i] = Math.round(canvas.toScreenX(polygon.getPointX(i)));
            py[i] = Math.round(canvas.toScreenY(polygon.getPointY(i)));
        }

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(alpha);
        g2.setColor(convertColor(color));
        g2.fillPolygon(px, py, polygon.getNumPoints());
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
        drawImage(((AWTImage) image).getImage(), Math.round(x), Math.round(y), transform);
    }

    private void drawImage(BufferedImage image, int x, int y, Transform transform) {
        if (transform == null) {
            transform = NULL_TRANSFORM;
        }

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform);

        AffineTransform transform2D = applyTransform(x, y, image.getWidth(), image.getHeight(), transform);
        g2.drawImage(image, transform2D, null);

        if (transform.getMask() != null) {
            BufferedImage maskImage = prepareMaskImage(image, transform.getMask());
            g2.drawImage(maskImage, transform2D, null);
        }

        g2.setComposite(originalComposite);
    }

    @Override
    public void drawText(String text, TTFont font, float x, float y, Align align, AlphaTransform alpha) {
        float screenX = canvas.toScreenX(x);
        float screenY = canvas.toScreenY(y);

        float normalizedFontSize = Math.round(canvas.getZoomLevel() * font.getSize());
        Font awtFont = mediaLoader.getFont(font).deriveFont(font.isBold() ? Font.BOLD : Font.PLAIN,
            normalizedFontSize);
        int estimatedWidth = g2.getFontMetrics(awtFont).stringWidth(text);

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(alpha);

        g2.setColor(convertColor(font.getColor()));
        g2.setFont(awtFont);

        if (align == Align.CENTER) {
            g2.drawString(text, screenX - estimatedWidth / 2f, screenY);
        } else if (align == Align.RIGHT) {
            g2.drawString(text, screenX - estimatedWidth, screenY);
        } else {
            g2.drawString(text, screenX, screenY);
        }

        g2.setComposite(originalComposite);
    }

    private AffineTransform applyTransform(int x, int y, int width, int height, Transform transform) {
        float screenX = canvas.toScreenX(x);
        float screenY = canvas.toScreenY(y);
        float scaleX = canvas.getZoomLevel() * (transform.getScaleX() / 100f);
        float scaleY = canvas.getZoomLevel() * (transform.getScaleY() / 100f);
        int screenWidth = (int) (width * scaleX);
        int screenHeight = (int) (height * scaleY);

        AffineTransform transform2D = new AffineTransform();
        transform2D.setToIdentity();
        transform2D.translate(screenX - screenWidth / 2f, screenY - screenHeight / 2f);
        transform2D.rotate(transform.getRotationInRadians(), screenWidth / 2.0, screenHeight / 2.0);
        transform2D.scale(scaleX, scaleY);
        return transform2D;
    }

    private void applyAlphaComposite(AlphaTransform transform) {
        if (transform != null && transform.getAlpha() != 100) {
            Composite alphaComposite = AlphaComposite.SrcOver.derive(transform.getAlpha() / 100f);
            g2.setComposite(alphaComposite);
        }
    }

    private Color convertColor(ColorRGB color) {
        Color result = colorCache.get(color);
        if (result == null) {
            result = new Color(color.getR(), color.getG(), color.getB());
            colorCache.put(color, result);
        }
        return result;
    }

    private BufferedImage prepareMaskImage(BufferedImage original, ColorRGB mask) {
        String cacheKey = original.getWidth() + "x" + original.getHeight();
        BufferedImage maskImage = maskCache.get(cacheKey);

        if (maskImage == null) {
            maskImage = new BufferedImage(original.getWidth(), original.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
            maskCache.put(cacheKey, maskImage);
        }

        Graphics2D g2 = Utils2D.createGraphics(maskImage, true, false);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, maskImage.getWidth(), maskImage.getHeight());
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(convertColor(mask));
        g2.drawImage(original, 0, 0, original.getWidth(), original.getHeight(), null);
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.fillRect(0, 0, maskImage.getWidth(), maskImage.getHeight());
        g2.dispose();

        return maskImage;
    }
}
