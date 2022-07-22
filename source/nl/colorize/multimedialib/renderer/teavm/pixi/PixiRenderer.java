//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm.pixi;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Graphic2D;
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
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.TeaImage;
import nl.colorize.multimedialib.scene.Layer;
import nl.colorize.multimedialib.scene.Stage;
import nl.colorize.multimedialib.scene.StageObserver;
import nl.colorize.multimedialib.scene.StageVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders graphics using the <a href="https://pixijs.com">PixiJS</a> JavaScript
 * library. Depending on the platform and browser, PixiJS will either use WebGL
 * or fall back to the HTML canvas API.
 */
public class PixiRenderer implements StageVisitor, StageObserver {

    private Canvas canvas;
    private PixiInterface pixi;
    private Map<Graphic2D, PixiDisplayObject> displayObjects;

    private Graphic2D currentGraphic;
    private PixiDisplayObject currentDisplayObject;
    private int zIndex;

    public PixiRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.displayObjects = new HashMap<>();
        this.pixi = Browser.initPixiInterface();
    }

    @Override
    public void preVisitStage(Stage stage) {
        currentGraphic = null;
        zIndex = 1;
    }

    @Override
    public void preVisitGraphic(Graphic2D graphic, boolean visible) {
        currentGraphic = graphic;
        currentDisplayObject = displayObjects.get(currentGraphic);
        currentDisplayObject.setVisible(visible);
    }

    @Override
    public void drawBackground(ColorRGB color) {
        pixi.changeBackgroundColor(color.getRGB());
    }

    @Override
    public void drawSprite(Sprite sprite) {
        Transform transform = sprite.getTransform();

        currentDisplayObject.setX(toScreenX(sprite.getPosition()));
        currentDisplayObject.setY(toScreenY(sprite.getPosition()));
        currentDisplayObject.setAlpha(transform.getAlpha() / 100f);
        currentDisplayObject.setAngle(transform.getRotation());
        currentDisplayObject.getScale().setX(transform.getScaleX() / 100f);
        currentDisplayObject.getScale().setY(transform.getScaleY() / 100f);

        TeaImage image = getImage(sprite);
        PixiTexture texture = currentDisplayObject.getTexture();
        texture.getFrame().setX(image.getRegion().getX());
        texture.getFrame().setY(image.getRegion().getY());
        texture.getFrame().setWidth(image.getRegion().getWidth());
        texture.getFrame().setHeight(image.getRegion().getHeight());
        texture.updateUvs();

        updateTint(sprite.getTransform().getMask());
    }

    private void updateTint(ColorRGB tintColor) {
        if (!currentDisplayObject.isTintEnabled() && tintColor != null) {
            pixi.applyTint(currentDisplayObject, tintColor.getRGB());
        } else if (currentDisplayObject.isTintEnabled() && tintColor == null) {
            pixi.clearTint(currentDisplayObject);
        }
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        currentDisplayObject.clear();
        currentDisplayObject.lineStyle(line.getThickness(), graphic.getColor().getRGB());
        currentDisplayObject.moveTo(toScreenX(line.getStart()), toScreenY(line.getStart()));
        currentDisplayObject.lineTo(toScreenX(line.getEnd()), toScreenY(line.getEnd()));

        currentDisplayObject.setAlpha(graphic.getAlpha() / 100f);
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        currentDisplayObject.clear();
        currentDisplayObject.beginFill(graphic.getColor().getRGB(), 1f);
        currentDisplayObject.drawRect(
            toScreenX(rect.getX()),
            toScreenY(rect.getY()),
            rect.getWidth() * canvas.getZoomLevel(),
            rect.getHeight() * canvas.getZoomLevel()
        );
        currentDisplayObject.endFill();

        currentDisplayObject.setAlpha(graphic.getAlpha() / 100f);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        currentDisplayObject.clear();
        currentDisplayObject.beginFill(graphic.getColor().getRGB(), 1f);
        currentDisplayObject.drawCircle(
            toScreenX(circle.getCenterX()),
            toScreenY(circle.getCenterY()),
            circle.getRadius() * canvas.getZoomLevel()
        );
        currentDisplayObject.endFill();

        currentDisplayObject.setAlpha(graphic.getAlpha() / 100f);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        float[] points = new float[polygon.getNumPoints() * 2];
        for (int i = 0; i < polygon.getNumPoints(); i++) {
            points[i * 2] = toScreenX(polygon.getPointX(i));
            points[i * 2 + 1] = toScreenY(polygon.getPointY(i));
        }

        currentDisplayObject.clear();
        currentDisplayObject.beginFill(graphic.getColor().getRGB(), 1f);
        currentDisplayObject.drawPolygon(points);
        currentDisplayObject.endFill();

        currentDisplayObject.setAlpha(graphic.getAlpha() / 100f);
    }

    @Override
    public void drawText(Text text) {
        currentDisplayObject.setText(text.getText());
        currentDisplayObject.setX(toScreenX(text.getPosition()));
        currentDisplayObject.setY(toScreenY(text.getPosition()));
        currentDisplayObject.setAlpha(text.getAlpha() / 100f);
    }

    private float toScreenX(float x) {
        return canvas.toScreenX(x);
    }

    private float toScreenX(Point2D point) {
        return canvas.toScreenX(point.getX());
    }

    private float toScreenY(float y) {
        return canvas.toScreenY(y);
    }

    private float toScreenY(Point2D point) {
        return canvas.toScreenY(point.getY());
    }

    @Override
    public void onGraphicAdded(Layer layer, Graphic2D graphic) {
        if (!pixi.hasLayer(layer.getName())) {
            pixi.createLayer(layer.getName());
        }

        zIndex++;
        PixiDisplayObject displayObject = createDisplayObject(layer, graphic);
        displayObject.setZIndex(zIndex);
        displayObjects.put(graphic, displayObject);
    }

    private PixiDisplayObject createDisplayObject(Layer layer, Graphic2D graphic) {
        if (graphic instanceof Sprite) {
            TeaImage image = getImage((Sprite) graphic);
            Rect region = image.getRegion();
            return pixi.createSprite(layer.getName(), image.getId(),
                region.getX(), region.getY(), region.getWidth(), region.getHeight());
        } else if (graphic instanceof Primitive) {
            return pixi.createGraphics(layer.getName());
        } else if (graphic instanceof Text) {
            Text text = (Text) graphic;
            TTFont font = text.getFont();
            return pixi.createText(layer.getName(), font.family(), font.size(), font.bold(),
                text.getAlign().toString(), font.getLineHeight(), font.color().getRGB());
        } else {
            throw new IllegalArgumentException("Unknown graphics type: " + graphic);
        }
    }

    private TeaImage getImage(Sprite sprite) {
        return (TeaImage) sprite.getCurrentGraphics();
    }

    @Override
    public void onGraphicRemoved(Layer layer, Graphic2D graphic) {
        PixiDisplayObject displayObject = displayObjects.get(graphic);
        if (displayObject != null) {
            pixi.removeDisplayObject(layer.getName(), displayObject);
        }
    }

    @Override
    public void onStageCleared() {
        pixi.clearStage();
    }
}
