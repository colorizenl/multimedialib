//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transformable;
import nl.colorize.util.stats.Cache;

import java.util.List;

/**
 * Renders graphics to a hardware-accelerated JavaFX {@link Canvas}.
 */
public class JFXGraphics implements StageVisitor {

    private DisplayMode displayMode;
    private JFXMediaLoader media;
    private Cache<MaskImageCacheKey, WritableImage> maskImageCache;

    private Canvas fxCanvas;
    private GraphicsContext gc;

    public JFXGraphics(DisplayMode displayMode, JFXMediaLoader media) {
        this.displayMode = displayMode;
        this.media = media;
        this.maskImageCache = Cache.from(this::createMaskImage);
    }

    /**
     * Initializes this class using the specified JavaFX canvas. This method
     * must be called from the JavaFX application thread.
     */
    protected void init(Canvas fxCanvas) {
        this.fxCanvas = fxCanvas;
        this.gc = fxCanvas.getGraphicsContext2D();
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
    public void drawBackground(ColorRGB color) {
        gc.setFill(toColor(color));
        gc.fillRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
    }

    @Override
    public void drawSprite(Sprite sprite) {
        JFXImage image = (JFXImage) sprite.getCurrentGraphics();
        Image fxImage = image.getImage();
        Region region = image.getRegion();
        Transformable transform = sprite.getGlobalTransform();
        float zoom = displayMode.canvas().getZoomLevel();

        if (transform.getMaskColor() != null) {
            MaskImageCacheKey key = new MaskImageCacheKey(fxImage, transform.getMaskColor());
            fxImage = maskImageCache.get(key);
        }

        gc.setGlobalAlpha(transform.getAlpha() / 100.0);
        gc.translate(toScreenX(transform.getX()), toScreenY(transform.getY()));
        gc.rotate(transform.getRotation().degrees());
        gc.scale((transform.getScaleX() * zoom) / 100.0, (transform.getScaleY() * zoom) / 100.0);
        gc.drawImage(fxImage, region.x(), region.y(), region.width(), region.height(),
            -region.width() / 2f, -region.height() / 2f, region.width(), region.height());
        gc.setGlobalAlpha(1.0);
        gc.setTransform(1, 0, 0, 1, 0, 0);
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        Transformable transform = graphic.getGlobalTransform();

        gc.setStroke(toColor(graphic.getColor(), transform.getAlpha()));
        gc.beginPath();
        gc.moveTo(toScreenX(line.start().x()), toScreenY(line.start().y()));
        gc.lineTo(toScreenX(line.end().x()), toScreenY(line.end().y()));
        gc.closePath();
        gc.stroke();
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line) {
        List<Point2D> points = line.points();
        Transformable transform = graphic.getGlobalTransform();

        gc.setStroke(toColor(graphic.getColor(), transform.getAlpha()));
        gc.beginPath();
        gc.moveTo(toScreenX(points.getFirst().x()), toScreenY(points.getFirst().y()));
        for (int i = 1; i < points.size(); i++) {
            gc.lineTo(toScreenX(points.get(i).x()), toScreenY(points.get(i).y()));
        }
        gc.closePath();
        gc.stroke();
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        Transformable transform = graphic.getGlobalTransform();
        float screenX = toScreenX(rect.x());
        float screenY = toScreenY(rect.y());
        float screenWidth = toScreenX(rect.getEndX()) - screenX;
        float screenHeight = toScreenY(rect.getEndY()) - screenY;

        gc.setFill(toColor(graphic.getColor(), transform.getAlpha()));
        gc.fillRect(screenX, screenY, screenWidth, screenHeight);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        Transformable transform = graphic.getGlobalTransform();
        float screenX0 = toScreenX(circle.center().x() - circle.radius());
        float screenY0 = toScreenY(circle.center().y() - circle.radius());
        float screenX1 = toScreenX(circle.center().x() + circle.radius());
        float screenY1 = toScreenY(circle.center().y() + circle.radius());

        gc.setFill(toColor(graphic.getColor(), transform.getAlpha()));
        gc.fillOval(screenX0, screenY0, screenX1 - screenX0, screenY1 - screenY0);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        Transformable transform = graphic.getGlobalTransform();
        double[] screenX = new double[polygon.getNumPoints()];
        double[] screenY = new double[polygon.getNumPoints()];

        for (int i = 0; i < polygon.getNumPoints(); i++) {
            screenX[i] = toScreenX(polygon.getPointX(i));
            screenX[i] = toScreenY(polygon.getPointY(i));
        }

        gc.setFill(toColor(graphic.getColor(), transform.getAlpha()));
        gc.fillPolygon(screenX, screenY, polygon.getNumPoints());
    }

    @Override
    public void drawText(Text text) {
        FontFace scaled = text.getFont().scale(displayMode.canvas());
        Font font = media.getFont(scaled);
        Transformable transform = text.getGlobalTransform();

        gc.setFont(font);
        gc.setTextAlign(toTextAlignment(text.getAlign()));
        gc.setFill(toColor(scaled.style().color()));
        gc.setGlobalAlpha(transform.getAlpha() / 100.0);

        text.forLines((i, line) -> {
            float screenX = toScreenX(transform.getX());
            float screenY = toScreenY(transform.getY() + i * text.getLineHeight());
            gc.fillText(line, screenX, screenY);
        });

        gc.setGlobalAlpha(1.0);
    }

    private WritableImage createMaskImage(MaskImageCacheKey key) {
        int width = (int) key.image.getWidth();
        int height = (int) key.image.getHeight();
        WritableImage maskImage = new WritableImage(width, height);

        PixelReader pixelReader = key.image.getPixelReader();
        PixelWriter pixelWriter = maskImage.getPixelWriter();
        Color maskColor = toColor(key.mask);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color originalColor = pixelReader.getColor(x, y);
                if (originalColor.getOpacity() > 0.0) {
                    pixelWriter.setColor(x, y, maskColor);
                }
            }
        }

        return maskImage;
    }

    private float toScreenX(float canvasX) {
        return displayMode.canvas().toScreenX(canvasX);
    }

    private float toScreenY(float canvasY) {
        return displayMode.canvas().toScreenY(canvasY);
    }

    private Color toColor(ColorRGB rgb) {
        return toColor(rgb, 100f);
    }

    private Color toColor(ColorRGB rgb, float alpha) {
        return new Color(rgb.r() / 255.0, rgb.g() / 255.0, rgb.b() / 255.0, alpha / 100.0);
    }

    private TextAlignment toTextAlignment(Align align) {
        return switch (align) {
            case LEFT -> TextAlignment.LEFT;
            case CENTER -> TextAlignment.CENTER;
            case RIGHT -> TextAlignment.RIGHT;
        };
    }

    /**
     * Caching mask images is based on the full original image, not the
     * sub-image or region represented by a {@link JFXImage} instance.
     */
    private record MaskImageCacheKey(Image image, ColorRGB mask) {
    }
}
