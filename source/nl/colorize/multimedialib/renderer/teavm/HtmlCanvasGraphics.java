//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import lombok.Getter;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * Renders graphics using the HTML canvas API. The current platform and browser
 * will influence which drawing operations are hardware-accelerated.
 */
public class HtmlCanvasGraphics implements TeaGraphics {

    private Canvas sceneCanvas;
    private TeaMediaLoader mediaLoader;
    @Getter private HTMLCanvasElement htmlCanvas;
    private CanvasRenderingContext2D context;

    public HtmlCanvasGraphics(Canvas sceneCanvas) {
        this.sceneCanvas = sceneCanvas;
    }

    @Override
    public void init(TeaMediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;

        HTMLDocument document = Window.current().getDocument();
        HTMLElement container = document.getElementById("multimediaLibContainer");
        htmlCanvas = createFullScreenCanvas(container);
        context = (CanvasRenderingContext2D) htmlCanvas.getContext("2d");
    }

    private HTMLCanvasElement createFullScreenCanvas(HTMLElement container) {
        Window window = Window.current();
        HTMLDocument document = window.getDocument();

        HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
        container.appendChild(canvas);
        resizeCanvas(canvas, container);
        window.addEventListener("resize", e -> resizeCanvas(canvas, container));

        return canvas;
    }

    private void resizeCanvas(HTMLCanvasElement canvas, HTMLElement container) {
        HTMLDocument document = Window.current().getDocument();
        int width = container.getOffsetWidth();
        int height = document.getDocumentElement().getClientHeight();

        resizeCanvas(canvas, width, height);
    }

    private void resizeCanvas(HTMLCanvasElement canvas, int width, int height) {
        Window window = Window.current();
        float devicePixelRatio = (float) window.getDevicePixelRatio();

        canvas.getStyle().setProperty("width", width + "px");
        canvas.getStyle().setProperty("height", height + "px");
        canvas.setWidth(Math.round(width * devicePixelRatio));
        canvas.setHeight(Math.round(height * devicePixelRatio));
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
    public boolean shouldVisitAllGraphics() {
        return false;
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
    }

    @Override
    public void prepareStage(Stage stage) {
        context.clearRect(0f, 0f, htmlCanvas.getWidth(), htmlCanvas.getHeight());
    }

    @Override
    public void drawBackground(ColorRGB color) {
        context.setFillStyle(color.toHex());
        context.fillRect(0f, 0f, htmlCanvas.getWidth(), htmlCanvas.getHeight());
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        TeaImage teaImage = (TeaImage) sprite.getCurrentGraphics();

        if (teaImage.getWidth() > 0f && teaImage.getHeight() > 0f) {
            drawImage(teaImage, teaImage.getRegion(), globalTransform);
        }
    }

    private void drawImage(TeaImage image, Region region, Transform transform) {
        CanvasImageSource source = prepareImage(image, transform.getMaskColor());
        Point2D position = transform.getPosition();

        if (source == null) {
            // Image is still loading, try again next frame.
            return;
        }

        context.setGlobalAlpha(transform.getAlpha() / 100f);
        context.translate(toScreenX(position), toScreenY(position));
        context.rotate(transform.getRotation().degrees() * Math.PI / 180f);
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
            return mediaLoader.applyMask(image, mask);
        }
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        context.setStrokeStyle(graphic.getColor().toHex());
        context.setLineWidth(graphic.getStroke());
        context.beginPath();
        context.moveTo(toScreenX(line.start()), toScreenY(line.start()));
        context.lineTo(toScreenX(line.end()), toScreenY(line.end()));
        context.stroke();
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        context.setStrokeStyle(graphic.getColor().toHex());
        context.setLineWidth(graphic.getStroke());
        context.beginPath();
        context.moveTo(toScreenX(line.getHead()), toScreenY(line.getHead()));
        for (Point2D p : line.points()) {
            context.lineTo(toScreenX(p), toScreenY(p));
        }
        context.stroke();
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransformt) {
        context.setGlobalAlpha(globalTransformt.getAlpha() / 100f);
        context.setFillStyle(graphic.getColor().toHex());
        context.fillRect(
            toScreenX(rect.x()),
            toScreenY(rect.y()),
            rect.width() * sceneCanvas.getZoomLevel(),
            rect.height() * sceneCanvas.getZoomLevel()
        );
        context.setGlobalAlpha(1f);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        context.setGlobalAlpha(globalTransform.getAlpha() / 100f);
        context.setFillStyle(graphic.getColor().toHex());
        context.beginPath();
        context.arc(toScreenX(circle.center().x()), toScreenY(circle.center().y()),
            circle.radius() * sceneCanvas.getZoomLevel(), 0f, 2f * Math.PI);
        context.fill();
        context.setGlobalAlpha(1f);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        context.setGlobalAlpha(globalTransform.getAlpha() / 100f);
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
    public void drawText(Text text, Transform globalTransform) {
        FontFace font = text.getFont().scale(sceneCanvas);

        context.setGlobalAlpha(globalTransform.getAlpha() / 100f);
        context.setFont(getFontString(font));
        context.setFillStyle(font.color().toHex());
        context.setTextAlign(text.getAlign().toString().toLowerCase());
        text.forLines((i, line) -> {
            float y = toScreenY(globalTransform.getPosition().y() + i * text.getLineHeight());
            context.fillText(line, toScreenX(globalTransform.getPosition()), y);
        });
        context.setGlobalAlpha(1f);
    }

    private String getFontString(FontFace font) {
        return font.size() + "px " + font.family();
    }

    private float toScreenX(float x) {
        return sceneCanvas.toScreenX(x);
    }

    private float toScreenX(Point2D point) {
        return sceneCanvas.toScreenX(point.x());
    }

    private float toScreenY(float y) {
        return sceneCanvas.toScreenY(y);
    }

    private float toScreenY(Point2D point) {
        return sceneCanvas.toScreenY(point.y());
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }
}
