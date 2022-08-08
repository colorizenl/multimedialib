//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.FontStyle;
import nl.colorize.multimedialib.graphics.Text;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.StageVisitor;
import org.teavm.jso.canvas.CanvasImageSource;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

/**
 * Renders graphics using the HTML canvas API. The current platform and browser
 * will influence which drawing operations are hardware-accelerated.
 */
public class CanvasRenderer implements StageVisitor {

    private Canvas sceneCanvas;
    private HTMLCanvasElement htmlCanvas;
    private CanvasRenderingContext2D context;

    protected CanvasRenderer(Canvas sceneCanvas) {
        this.sceneCanvas = sceneCanvas;
        this.htmlCanvas = Browser.getCanvas();
        this.context = (CanvasRenderingContext2D) htmlCanvas.getContext("2d");
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
        Rect region = teaImage.getRegion();

        if (region.getWidth() > 0f && region.getHeight() > 0f) {
            drawImage(teaImage, region, sprite.getPosition(), sprite.getTransform());
        }
    }

    private void drawImage(TeaImage image, Rect region, Point2D position, Transform transform) {
        String mask = transform.getMask() != null ? transform.getMask().toHex() : null;
        CanvasImageSource source = Browser.prepareImage(image.getId(), mask);

        context.setGlobalAlpha(transform.getAlpha() / 100f);
        context.translate(toScreenX(position), toScreenY(position));
        context.rotate(transform.getRotation() * Math.PI / 180f);
        context.scale((transform.getScaleX() * sceneCanvas.getZoomLevel()) / 100f,
            (transform.getScaleY() * sceneCanvas.getZoomLevel()) / 100f);
        context.drawImage(source, region.getX(), region.getY(), region.getWidth(), region.getHeight(),
            -region.getWidth() / 2f, -region.getHeight() / 2f, region.getWidth(), region.getHeight());
        context.setGlobalAlpha(1f);
        context.setTransform(1, 0, 0, 1, 0, 0);
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
        context.setGlobalAlpha(graphic.getAlpha() / 100f);
        context.setFillStyle(graphic.getColor().toHex());
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
        String fontString = (style.bold() ? "bold " : "") + style.size() + "px " + style.family();

        context.setGlobalAlpha(text.getAlpha() / 100f);
        context.setFillStyle(style.color().toHex());
        context.setFont(fontString);
        context.setTextAlign(text.getAlign().toString().toLowerCase());
        text.forLines((i, line) -> {
            float y = toScreenY(text.getPosition().getY() + i * text.getLineHeight(sceneCanvas));
            context.fillText(line, toScreenX(text.getPosition()), y);
        });
        context.setGlobalAlpha(1f);
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
}
