//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transformable;
import nl.colorize.util.stats.Cache;
import nl.colorize.util.swing.Utils2D;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Uses Java 2D to render graphics. Because of Java 2D's flexibility this class
 * supports several graphics contexts: drawing can be either directly to a
 * window using active rendering, but also to a Swing component, or to an image.
 */
public class Java2DGraphicsContext implements StageVisitor {

    private Canvas canvas;
    private Graphics2D g2;

    private Cache<ColorRGB, Color> colorCache;
    private Cache<MaskImage, BufferedImage> maskCache;
    private Cache<CircleImage, BufferedImage> circleCache;
    private Cache<FontFace, Font> fontCache;

    private static final int CACHE_CAPACITY = 1000;

    protected Java2DGraphicsContext(Canvas canvas, Cache<FontFace, Font> fontCache) {
        this.canvas = canvas;

        this.colorCache = Cache.from(this::convertColor, CACHE_CAPACITY);
        this.maskCache = Cache.from(MaskImage::render, CACHE_CAPACITY);
        this.circleCache = Cache.from(CircleImage::render, CACHE_CAPACITY);
        this.fontCache = fontCache;
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

    @Override
    public void prepareStage(Stage stage) {
    }

    @Override
    public boolean shouldVisitAllGraphics() {
        return false;
    }

    @Override
    public void visitContainer(Container container) {
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        float width = canvas.toScreenX(canvas.getWidth());
        float height = canvas.toScreenY(canvas.getHeight());

        g2.setColor(colorCache.get(backgroundColor));
        g2.fillRect(0, 0, Math.round(width), Math.round(height) + 30);
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        float x0 = canvas.toScreenX(line.start().x());
        float y0 = canvas.toScreenY(line.start().y());
        float x1 = canvas.toScreenX(line.end().x());
        float y1 = canvas.toScreenY(line.end().y());

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(graphic.getTransform().getAlpha());
        g2.setStroke(new BasicStroke(graphic.getStroke()));
        g2.setColor(colorCache.get(graphic.getColor()));
        g2.drawLine(Math.round(x0), Math.round(y0), Math.round(x1), Math.round(y1));
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line) {
        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(graphic.getTransform().getAlpha());
        g2.setStroke(new BasicStroke(graphic.getStroke()));
        g2.setColor(colorCache.get(graphic.getColor()));

        for (Line segment : line.getSegments()) {
            float x0 = canvas.toScreenX(segment.start().x());
            float y0 = canvas.toScreenY(segment.start().y());
            float x1 = canvas.toScreenX(segment.end().x());
            float y1 = canvas.toScreenY(segment.end().y());

            g2.drawLine(Math.round(x0), Math.round(y0), Math.round(x1), Math.round(y1));
        }

        g2.setComposite(originalComposite);
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        Transformable transform = graphic.getGlobalTransform();
        float screenX = canvas.toScreenX(rect.x());
        float screenY = canvas.toScreenY(rect.y());
        float screenWidth = canvas.toScreenX(rect.getEndX()) - screenX;
        float screenHeight = canvas.toScreenY(rect.getEndY()) - screenY;

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform.getAlpha());
        g2.setColor(colorCache.get(graphic.getColor()));
        g2.fillRect(Math.round(screenX), Math.round(screenY), Math.round(screenWidth),
            Math.round(screenHeight));
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        CircleImage key = new CircleImage(circle.radius(), colorCache.get(graphic.getColor()));
        BufferedImage image = circleCache.get(key);

        Transform transform = new Transform();
        transform.setPosition(circle.center());
        transform.setAlpha(graphic.getGlobalTransform().getAlpha());

        drawImage(image, transform);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        Transformable transform = graphic.getGlobalTransform();
        int[] px = new int[polygon.getNumPoints()];
        int[] py = new int[polygon.getNumPoints()];

        for (int i = 0; i < polygon.getNumPoints(); i++) {
            px[i] = Math.round(canvas.toScreenX(polygon.getPointX(i)));
            py[i] = Math.round(canvas.toScreenY(polygon.getPointY(i)));
        }

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform.getAlpha());
        g2.setColor(colorCache.get(graphic.getColor()));
        g2.fillPolygon(px, py, polygon.getNumPoints());
        g2.setComposite(originalComposite);
    }

    @Override
    public void drawSprite(Sprite sprite) {
        AWTImage image = (AWTImage) sprite.getCurrentGraphics();
        drawImage(image.getImage(), sprite.getGlobalTransform());
    }

    private void drawImage(BufferedImage image, Transformable transform) {
        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform.getAlpha());

        AffineTransform transform2D = applyTransform(transform, image.getWidth(), image.getHeight());
        g2.drawImage(image, transform2D, null);

        if (transform.getMaskColor() != null) {
            MaskImage key = new MaskImage(image, colorCache.get(transform.getMaskColor()));
            g2.drawImage(maskCache.get(key), transform2D, null);
        }

        g2.setComposite(originalComposite);
    }

    @Override
    public void drawText(Text text) {
        Font font = fontCache.get(text.getFont().scale(canvas));
        ColorRGB color = text.getFont().style().color();
        Transformable transform = text.getGlobalTransform();

        Composite originalComposite = g2.getComposite();
        applyAlphaComposite(transform.getAlpha());

        g2.setColor(colorCache.get(color));
        g2.setFont(font);
        drawLines(text.getLines(), transform.getPosition(), text.getAlign(), text.getLineHeight());
        g2.setComposite(originalComposite);
    }

    private void drawLines(List<String> lines, Point2D position, Align align, float lineHeight) {
        for (int i = 0; i < lines.size(); i++) {
            float screenX = canvas.toScreenX(position.x());
            float screenY = canvas.toScreenY(position.y() + i * lineHeight);
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

    private AffineTransform applyTransform(Transformable transform, int width, int height) {
        float screenX = canvas.toScreenX(transform.getPosition());
        float screenY = canvas.toScreenY(transform.getPosition());

        float scaleX = canvas.getZoomLevel() * (transform.getScaleX() / 100f);
        float scaleY = canvas.getZoomLevel() * (transform.getScaleY() / 100f);

        int screenWidth = (int) (width * scaleX);
        int screenHeight = (int) (height * scaleY);

        AffineTransform transform2D = new AffineTransform();
        transform2D.setToIdentity();
        transform2D.translate(screenX - screenWidth / 2f, screenY - screenHeight / 2f);
        transform2D.rotate(transform.getRotation().getRadians(), screenWidth / 2.0, screenHeight / 2.0);
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
        return new Color(color.r(), color.g(), color.b());
    }

    /**
     * Java2D does not use hardware acceleration for certain drawing operations.
     * This can have a significant performance impact, so shapes are rendered
     * to an image, then that image is drawn by the renderer.
     */
    private record CircleImage(float radius, Color color) {

        public BufferedImage render() {
            int size = Math.round(radius * 2f);
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = Utils2D.createGraphics(image, true, false);
            g2.setColor(color);
            g2.fillOval(0, 0, size, size);
            g2.dispose();
            return image;
        }
    }

    /**
     * Cache key for images with a mask/tint transform. This is an expensive
     * operation in Java2D since it needs to create a new image that combines
     * the original image with the mask.
     */
    private record MaskImage(BufferedImage original, Color maskColor) {

        public BufferedImage render() {
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = Utils2D.createGraphics(image, true, false);
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(maskColor);
            g2.drawImage(original, 0, 0, original.getWidth(), original.getHeight(), null);
            g2.setComposite(AlphaComposite.SrcAtop);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.dispose();
            return image;
        }
    }
}
