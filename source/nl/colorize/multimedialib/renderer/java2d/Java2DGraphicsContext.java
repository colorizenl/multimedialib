//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.math.SimpleCache;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.StageVisitor;
import nl.colorize.util.swing.Utils2D;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Uses Java 2D to render graphics. Because of Java 2D's flexibility this class
 * supports several graphics contexts: drawing can be either directly to a
 * window using active rendering, but also to a Swing component, or to an image.
 */
public class Java2DGraphicsContext implements StageVisitor {

    private Canvas canvas;
    private Graphics2D g2;
    private StandardMediaLoader mediaLoader;

    private Map<ColorRGB, Color> colorCache;
    private Map<String, BufferedImage> maskCache;
    private SimpleCache<RenderedCircle, BufferedImage> circleCache;

    private static final Transform NULL_TRANSFORM = new Transform();
    private static final int SHAPE_CACHE_CAPACITY = 1000;

    public Java2DGraphicsContext(Canvas canvas, StandardMediaLoader mediaLoader) {
        this.canvas = canvas;
        this.mediaLoader = mediaLoader;

        this.colorCache = new HashMap<>();
        this.maskCache = new HashMap<>();
        this.circleCache = SimpleCache.create(RenderedCircle::render, SHAPE_CACHE_CAPACITY);
    }

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

    public void clear(int windowWidth, int windowHeight) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, windowWidth, windowHeight);
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        float width = canvas.toScreenX(canvas.getWidth());
        float height = canvas.toScreenY(canvas.getHeight());

        g2.setColor(convertColor(backgroundColor));
        g2.fillRect(0, 0, Math.round(width), Math.round(height) + 30);
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        float x0 = canvas.toScreenX(line.getStart().getX());
        float y0 = canvas.toScreenY(line.getStart().getY());
        float x1 = canvas.toScreenX(line.getEnd().getX());
        float y1 = canvas.toScreenY(line.getEnd().getY());

        g2.setStroke(new BasicStroke(line.getThickness()));
        g2.setColor(convertColor(graphic.getColor()));
        g2.drawLine(Math.round(x0), Math.round(y0), Math.round(x1), Math.round(y1));
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        float screenX = canvas.toScreenX(rect.getX());
        float screenY = canvas.toScreenY(rect.getY());
        float screenWidth = canvas.toScreenX(rect.getEndX()) - screenX;
        float screenHeight = canvas.toScreenY(rect.getEndY()) - screenY;

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(graphic.getAlpha());
        g2.setColor(convertColor(graphic.getColor()));
        g2.fillRect(Math.round(screenX), Math.round(screenY), Math.round(screenWidth),
            Math.round(screenHeight));
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        RenderedCircle key = new RenderedCircle(circle.getRadius(), graphic.getColor());
        BufferedImage image = circleCache.get(key);

        Transform transform = new Transform();
        transform.setAlpha(graphic.getAlpha());

        drawImage(image, circle.getCenter(), transform);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        int[] px = new int[polygon.getNumPoints()];
        int[] py = new int[polygon.getNumPoints()];

        for (int i = 0; i < polygon.getNumPoints(); i++) {
            px[i] = Math.round(canvas.toScreenX(polygon.getPointX(i)));
            py[i] = Math.round(canvas.toScreenY(polygon.getPointY(i)));
        }

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(graphic.getAlpha());
        g2.setColor(convertColor(graphic.getColor()));
        g2.fillPolygon(px, py, polygon.getNumPoints());
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawSprite(Sprite sprite) {
        BufferedImage image = ((AWTImage) sprite.getCurrentGraphics()).getImage();
        drawImage(image, sprite.getPosition(), sprite.getTransform());
    }

    private void drawImage(BufferedImage image, Point2D position, Transform transform) {
        if (transform == null) {
            transform = NULL_TRANSFORM;
        }

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform.getAlpha());

        AffineTransform transform2D = applyTransform(position, image.getWidth(),
            image.getHeight(), transform);
        g2.drawImage(image, transform2D, null);

        if (transform.getMask() != null) {
            BufferedImage maskImage = prepareMaskImage(image, transform.getMask());
            g2.drawImage(maskImage, transform2D, null);
        }

        g2.setComposite(originalComposite);
    }

    @Override
    public void drawText(Text text) {
        TTFont font = text.getFont();

        float normalizedFontSize = Math.round(canvas.getZoomLevel() * font.size());
        Font awtFont = mediaLoader.getFont(font).deriveFont(font.bold() ? Font.BOLD : Font.PLAIN,
            normalizedFontSize);

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(text.getAlpha());

        g2.setColor(convertColor(font.color()));
        g2.setFont(awtFont);
        drawLines(text.getLines(), text.getPosition(), text.getAlign(), font.getLineHeight());
        g2.setComposite(originalComposite);
    }

    private void drawLines(List<String> lines, Point2D position, Align align, int lineHeight) {
        for (int i = 0; i < lines.size(); i++) {
            float screenX = canvas.toScreenX(position.getX());
            float screenY = canvas.toScreenY(position.getY() + i * lineHeight);
            int estimatedWidth = g2.getFontMetrics().stringWidth(lines.get(i));

            if (align == Align.CENTER) {
                g2.drawString(lines.get(i), screenX - estimatedWidth / 2f, screenY);
            } else if (align == Align.RIGHT) {
                g2.drawString(lines.get(i), screenX - estimatedWidth, screenY);
            } else {
                g2.drawString(lines.get(i), screenX, screenY);
            }
        }
    }

    private AffineTransform applyTransform(Point2D position, int width, int height, Transform transform) {
        float screenX = canvas.toScreenX(position.getX());
        float screenY = canvas.toScreenY(position.getY());

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

    private void applyAlphaComposite(float alpha) {
        if (alpha != 100) {
            Composite alphaComposite = AlphaComposite.SrcOver.derive(alpha / 100f);
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

    /**
     * Java2D does not use hardware acceleration for certain drawing operations.
     * This can have a significant performance impact, so shapes are rendered
     * to an image, then that image is drawn by the renderer.
     */
    private static class RenderedCircle {

        private float radius;
        private ColorRGB color;

        public RenderedCircle(float radius, ColorRGB color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RenderedCircle) {
                RenderedCircle other = (RenderedCircle) o;
                return Math.abs(radius - other.radius) < Shape.EPSILON && color.equals(other.color);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(radius, color);
        }

        public BufferedImage render() {
            int size = Math.round(radius * 2f);
            Color rgba = new Color(color.getR(), color.getG(), color.getB());

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = Utils2D.createGraphics(image, true, false);
            g2.setColor(rgba);
            g2.fillOval(0, 0, size, size);
            g2.dispose();
            return image;
        }
    }
}
