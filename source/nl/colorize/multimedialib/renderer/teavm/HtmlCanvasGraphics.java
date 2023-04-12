//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.stats.Cache;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;

/**
 * Renders graphics using the HTML canvas API. The current platform and browser
 * will influence which drawing operations are hardware-accelerated.
 */
public class HtmlCanvasGraphics implements TeaGraphics {

    private Canvas sceneCanvas;
    private HTMLCanvasElement htmlCanvas;
    private CanvasRenderingContext2D context;

    private Cache<MaskImage, HTMLCanvasElement> maskImageCache;
    private String currentCanvasFont;

    private static final String QUERY_STRING = Browser.getPageQueryString();
    private static final boolean BOUNDS_TRACING_ENABLED = QUERY_STRING.contains("bounds-tracing");

    public HtmlCanvasGraphics(Canvas sceneCanvas) {
        this.sceneCanvas = sceneCanvas;

        this.maskImageCache = Cache.from(this::createMaskImage, 256);
        this.currentCanvasFont = "";
    }

    @Override
    public void init() {
        Window window = Window.current();
        HTMLDocument document = window.getDocument();

        htmlCanvas = (HTMLCanvasElement) document.createElement("canvas");
        context = (CanvasRenderingContext2D) htmlCanvas.getContext("2d");

        HTMLElement container = document.getElementById("multimediaLibContainer");
        container.appendChild(htmlCanvas);
        resizeCanvas(document, container);
        window.addEventListener("resize", e -> resizeCanvas(document, container));
    }

    private void resizeCanvas(HTMLDocument document, HTMLElement container) {
        int width = Math.round(container.getOffsetWidth());
        int height = Math.round(document.getDocumentElement().getClientHeight());

        htmlCanvas.getStyle().setProperty("width", width + "px");
        htmlCanvas.getStyle().setProperty("height", height + "px");
        htmlCanvas.setWidth(Math.round(width * getDevicePixelRatio()));
        htmlCanvas.setHeight(Math.round(height * getDevicePixelRatio()));
    }

    @Override
    public int getDisplayWidth() {
        return htmlCanvas.getWidth();
    }

    @Override
    public int getDisplayHeight() {
        return htmlCanvas.getHeight();
    }

    @Override
    public void drawBackground(ColorRGB color) {
        context.clearRect(0f, 0f, htmlCanvas.getWidth(), htmlCanvas.getHeight());
        context.setFillStyle(color.toHex());
        context.fillRect(0f, 0f, htmlCanvas.getWidth(), htmlCanvas.getHeight());
    }

    @Override
    public void drawSprite(Sprite sprite) {
        TeaImage teaImage = (TeaImage) sprite.getCurrentGraphics();

        if (teaImage.getWidth() > 0f && teaImage.getHeight() > 0f) {
            drawImage(teaImage, teaImage.getRegion(), sprite.getPosition(), sprite.getTransform());

            if (BOUNDS_TRACING_ENABLED) {
                drawRect(sprite.getBounds(), ColorRGB.RED, 50f);
            }
        }
    }

    private void drawImage(TeaImage image, Region region, Point2D position, Transform transform) {
        CanvasImageSource source = prepareImage(image, transform.getMask());

        if (source == null) {
            // Image is still loading, try again next frame.
            return;
        }

        context.setGlobalAlpha(transform.getAlpha() / 100f);
        context.translate(toScreenX(position), toScreenY(position));
        context.rotate(transform.getRotation() * Math.PI / 180f);
        context.scale((transform.getScaleX() * sceneCanvas.getZoomLevel()) / 100f,
            (transform.getScaleY() * sceneCanvas.getZoomLevel()) / 100f);
        context.drawImage(source, region.x(), region.y(), region.width(), region.height(),
            -region.width() / 2f, -region.height() / 2f, region.width(), region.height());
        context.setGlobalAlpha(1f);
        context.setTransform(1, 0, 0, 1, 0, 0);
    }

    private CanvasImageSource prepareImage(TeaImage image, ColorRGB mask) {
        if (!image.isLoaded()) {
            return null;
        } else if (mask == null) {
            return image.getImageElement().orElse(null);
        } else {
            MaskImage cacheKey = new MaskImage(image, mask);
            return maskImageCache.get(cacheKey);
        }
    }

    private HTMLCanvasElement createMaskImage(MaskImage key) {
        Preconditions.checkState(key.image.isLoaded(), "Image is still loading");

        HTMLDocument document = Window.current().getDocument();
        HTMLImageElement img = key.image().getImageElement().get();

        HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
        canvas.setWidth(img.getWidth());
        canvas.setHeight(img.getHeight());

        CanvasRenderingContext2D maskContext = (CanvasRenderingContext2D) canvas.getContext("2d");
        maskContext.drawImage(img, 0, 0, img.getWidth(), img.getHeight());
        maskContext.setGlobalCompositeOperation("source-atop");
        maskContext.setFillStyle(key.mask().toHex());
        maskContext.fillRect(0, 0, img.getWidth(), img.getHeight());

        return canvas;
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        context.setStrokeStyle(graphic.getColor().toHex());
        context.setLineWidth(line.getThickness());
        context.beginPath();
        context.moveTo(toScreenX(line.getStart()), toScreenY(line.getStart()));
        context.lineTo(toScreenX(line.getEnd()), toScreenY(line.getEnd()));
        context.stroke();
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        drawRect(rect, graphic.getColor(), graphic.getAlpha());
    }

    private void drawRect(Rect rect, ColorRGB color, float alpha) {
        context.setGlobalAlpha(alpha / 100f);
        context.setFillStyle(color.toHex());
        context.fillRect(toScreenX(rect.getX()), toScreenY(rect.getY()),
            rect.getWidth() * sceneCanvas.getZoomLevel(),
            rect.getHeight() * sceneCanvas.getZoomLevel());
        context.setGlobalAlpha(1f);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        context.setGlobalAlpha(graphic.getAlpha() / 100f);
        context.setFillStyle(graphic.getColor().toHex());
        context.beginPath();
        context.arc(toScreenX(circle.getCenterX()), toScreenY(circle.getCenterY()),
            circle.getRadius() * sceneCanvas.getZoomLevel(), 0f, 2f * Math.PI);
        context.fill();
        context.setGlobalAlpha(1f);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        context.setGlobalAlpha(graphic.getAlpha() / 100f);
        context.setFillStyle(graphic.getColor().toHex());
        context.beginPath();
        context.moveTo(toScreenX(polygon.getPointX(0)), toScreenY(polygon.getPointY(0)));
        for (int i = 1; i < polygon.getNumPoints(); i++) {
            context.lineTo(toScreenX(polygon.getPointX(i)), toScreenY(polygon.getPointY(i)));
        }
        context.fill();
        context.setGlobalAlpha(1f);
    }

    @Override
    public void drawText(Text text) {
        FontStyle style = text.getFont().scale(sceneCanvas).getStyle();
        drawText(text, style);
    }

    private void drawText(Text text, FontStyle style) {
        float lineHeight = text.getFont() != null ? text.getLineHeight() : style.size();

        changeCanvasFont(style);

        context.setGlobalAlpha(text.getAlpha() / 100f);
        context.setFillStyle(style.color().toHex());
        context.setTextAlign(text.getAlign().toString().toLowerCase());
        text.forLines((i, line) -> {
            float y = toScreenY(text.getPosition().getY() + i * lineHeight);
            context.fillText(line, toScreenX(text.getPosition()), y);
        });
        context.setGlobalAlpha(1f);
    }

    private void changeCanvasFont(FontStyle style) {
        String fontString = (style.bold() ? "bold " : "") + style.size() + "px " + style.family();

        if (!fontString.equals(currentCanvasFont)) {
            context.setFont(fontString);
            currentCanvasFont = fontString;
        }
    }

    private float toScreenX(float x) {
        return sceneCanvas.toScreenX(x);
    }

    private float toScreenX(Point2D point) {
        return sceneCanvas.toScreenX(point.getX());
    }

    private float toScreenY(float y) {
        return sceneCanvas.toScreenY(y);
    }

    private float toScreenY(Point2D point) {
        return sceneCanvas.toScreenY(point.getY());
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }

    /**
     * Used as a cache key for masking images, which is a pretty expensive
     * operation. The entire image is masked, not just the image region. If we
     * need a masked region, we just extract the corresponding region from the
     * masked image.
     */
    private record MaskImage(TeaImage image, ColorRGB mask) {
    }
}
