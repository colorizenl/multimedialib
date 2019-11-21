//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.graphics.Alignment;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.graphics.TrueTypeFont;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.util.swing.Utils2D;
import org.checkerframework.checker.units.qual.C;

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
public class Java2DGraphicsContext implements GraphicsContext {

    private Canvas canvas;
    private Graphics2D g2;
    private StandardMediaLoader mediaLoader;

    private Map<ColorRGB, Color> colorCache;
    private Map<String, BufferedImage> maskCache;

    private static final Transform NULL_TRANSFORM = new Transform();
    private static final Color CLEAR_COLOR = new Color(0, 0, 0, 0);

    protected Java2DGraphicsContext(Canvas canvas, StandardMediaLoader mediaLoader) {
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
        int width = canvas.toScreenX(canvas.getWidth());
        int height = canvas.toScreenY(canvas.getHeight());

        g2.setColor(convertColor(backgroundColor));
        g2.fillRect(0, 0, width, height);
    }

    @Override
    public void drawRect(Rect rect, ColorRGB color, Transform transform) {
        int screenX = canvas.toScreenX(rect.getX());
        int screenY = canvas.toScreenY(rect.getY());
        int screenWidth = canvas.toScreenX(rect.getEndX()) - screenX;
        int screenHeight = canvas.toScreenY(rect.getEndY()) - screenY;

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform);
        g2.setColor(convertColor(color));
        g2.fillRect(screenX, screenY, screenWidth, screenHeight);
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawPolygon(Polygon polygon, ColorRGB color, Transform transform) {
        int[] px = new int[polygon.getNumPoints()];
        int[] py = new int[polygon.getNumPoints()];

        for (int i = 0; i < polygon.getNumPoints(); i++) {
            px[i] = canvas.toScreenX(polygon.getPointX(i));
            py[i] = canvas.toScreenY(polygon.getPointY(i));
        }

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform);
        g2.setColor(convertColor(color));
        g2.fillPolygon(px, py, polygon.getNumPoints());
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawImage(Image image, float x, float y, Transform transform) {
        drawImage(((Java2DImage) image).getImage(), Math.round(x), Math.round(y), transform);
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
    public void drawText(String text, TrueTypeFont font, float x, float y, Alignment align) {
        int screenX = canvas.toScreenX(x);
        int screenY = canvas.toScreenY(y);
        Font awtFont = mediaLoader.getFont(font);
        int estimatedWidth = g2.getFontMetrics(awtFont).stringWidth(text);

        g2.setColor(convertColor(font.getColor()));
        g2.setFont(awtFont);

        if (align == Alignment.CENTER) {
            g2.drawString(text, screenX - estimatedWidth / 2f, screenY);
        } else if (align == Alignment.RIGHT) {
            g2.drawString(text, screenX - estimatedWidth, screenY);
        } else {
            g2.drawString(text, screenX, screenY);
        }
    }

    private boolean isTransformed(Transform transform) {
        if (transform == null) {
            return false;
        }

        return transform.isRotated() || transform.isScaled() ||
            transform.getAlpha() < 100 || transform.getMask() != null;
    }

    private AffineTransform applyTransform(int x, int y, int width, int height, Transform transform) {
        int screenX = canvas.toScreenX(x);
        int screenY = canvas.toScreenY(y);
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

    private void applyAlphaComposite(Transform transform) {
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
