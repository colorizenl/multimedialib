//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.skija;

import com.google.common.base.Preconditions;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.skija.Path;
import io.github.humbleui.skija.PathBuilder;
import io.github.humbleui.skija.Typeface;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.ImageTransform;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.Cache;
import nl.colorize.util.ResourceFile;

import java.util.List;

/**
 * Implementation of a 2D graphics renderer using the Skija library. Unlike
 * other parts of the renderer that partially depend on LWJGL, the graphics
 * really are "pure" Skija.
 */
public class SkijaGraphics implements StageVisitor {

    private Canvas skija;
    private RenderConfig config;
    private Cache<ResourceFile, Typeface> typefaceCache;

    private static final int FONT_CACHE_SIZE = 2048;
    private static final int CIRCLE_SEGMENTS = 32;

    protected SkijaGraphics(Canvas skija, RenderConfig config) {
        this.skija = skija;
        this.config = config;
        this.typefaceCache = Cache.from(this::loadTypeface, FONT_CACHE_SIZE);
    }

    private Typeface loadTypeface(ResourceFile file) {
        FontMgr fontManager = FontMgr.getDefault();
        Data ttfData = Data.makeFromBytes(file.readBytes());
        return fontManager.makeFromData(ttfData);
    }

    @Override
    public void prepareStage(Stage stage) {
        skija.resetMatrix();
    }

    @Override
    public void drawBackground(ColorRGB color) {
        skija.clear(color.getRGB());
    }

    @Override
    public void drawSprite(Sprite sprite, ImageTransform globalTransform) {
        SkijaImage image = (SkijaImage) sprite.getCurrentGraphics();
        drawImage(image, globalTransform);
    }

    private void drawImage(SkijaImage image, ImageTransform transform) {
        float screenX = toScreenX(transform.getPosition().x());
        float screenY = toScreenY(transform.getPosition().y());

        float scaleX = config.getCanvas().getZoomLevel() * (transform.getScaleX() / 100f);
        float scaleY = config.getCanvas().getZoomLevel() * (transform.getScaleY() / 100f);

        try (Paint paint = new Paint()) {
            paint.setAlpha(getAlpha(transform));

            skija.save();
            skija.resetMatrix();
            skija.translate(screenX, screenY);
            skija.translate(-image.getWidth() / 2f, -image.getHeight() / 2f);
            skija.scale(scaleX, scaleY);
            skija.drawImage(image.getImage(), 0f, 0f);
            skija.restore();
        }
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        List<Point2D> points = List.of(line.start(), line.end());
        drawLine(points, graphic.getStroke(), graphic.getColor(), globalTransform);
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        drawLine(line.points(), graphic.getStroke(), graphic.getColor(), globalTransform);
    }

    private void drawLine(List<Point2D> points, float stroke, ColorRGB color, Transform transform) {
        Preconditions.checkArgument(points.size() >= 2, "Invalid line");

        try (Paint paint = new Paint()) {
            paint.setColor(color.getRGB());
            paint.setAlpha(getAlpha(transform));
            paint.setStrokeWidth(stroke);
            paint.setMode(PaintMode.STROKE);

            try (Path path = buildPath(points, false)) {
                skija.drawPath(path, paint);
            }
        }
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        if (rect.width() == 0f || rect.height() == 0f) {
            return;
        }

        Polygon polygon = Polygon.createRectangle(rect.getCenter(), rect.width(), rect.height());
        drawPolygon(polygon.points(), graphic.getColor(), globalTransform);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        Polygon polygon = Polygon.createCircle(circle.getCenter(), circle.radius(), CIRCLE_SEGMENTS);
        drawPolygon(polygon.points(), graphic.getColor(), globalTransform);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        drawPolygon(polygon.points(), graphic.getColor(), globalTransform);
    }

    private void drawPolygon(List<Point2D> points, ColorRGB color, Transform transform) {
        Preconditions.checkArgument(points.size() >= 3, "Invalid polygon");

        try (Paint paint = new Paint()) {
            paint.setColor(color.getRGB());
            paint.setAlpha(getAlpha(transform));

            try (Path path = buildPath(points, true)) {
                skija.drawPath(path, paint);
            }
        }
    }

    private Path buildPath(List<Point2D> points, boolean close) {
        Preconditions.checkArgument(points.size() >= 2, "Invalid path");

        try (PathBuilder pathBuilder = new PathBuilder()) {
            pathBuilder.moveTo(toScreenX(points.getFirst().x()), toScreenY(points.getFirst().y()));
            for (int i = 1; i < points.size(); i++) {
                pathBuilder.lineTo(toScreenX(points.get(i).x()), toScreenY(points.get(i).y()));
            }
            if (close) {
                pathBuilder.closePath();
            }
            return pathBuilder.build();
        }
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        Typeface typeface = typefaceCache.get(text.getFont().origin());
        Font font = new Font(typeface, text.getFont().scale(config.getCanvas()).size());
        List<String> lines = text.getLines();

        try (Paint paint = new Paint()) {
            paint.setColor(text.getFont().color().getRGB());
            paint.setAlpha(getAlpha(globalTransform));

            for (int i = 0; i < lines.size(); i++) {
                float x = toScreenX(globalTransform.getPosition().x());
                float y = toScreenY(globalTransform.getPosition().y() + i * text.getLineHeight());
                float width = font.measureText(lines.get(i)).getWidth();

                switch (text.getAlign()) {
                    case LEFT -> skija.drawString(lines.get(i), x, y, font, paint);
                    case CENTER -> skija.drawString(lines.get(i), x - width / 2f, y, font, paint);
                    case RIGHT -> skija.drawString(lines.get(i), x - width, y, font, paint);
                }
            }
        }
    }

    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        throw new UnsupportedOperationException();
    }

    private int getAlpha(Transform transform) {
        return Math.round(transform.getAlpha() * 2.55f);
    }

    private float toScreenX(float canvasX) {
        return config.getCanvas().toScreenX(canvasX);
    }

    private float toScreenY(float canvasY) {
        return config.getCanvas().toScreenY(canvasY);
    }
}
