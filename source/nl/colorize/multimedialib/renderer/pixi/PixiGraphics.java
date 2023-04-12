//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.TeaGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaImage;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.FontStyle;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageObserver;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.LogHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Renders graphics using the <a href="https://pixijs.com">PixiJS</a> JavaScript
 * library. Depending on the platform and browser, PixiJS will either use WebGL
 * or fall back to the HTML canvas API.
 */
public class PixiGraphics implements TeaGraphics, StageObserver {

    private Canvas canvas;
    private PixiInterface pixi;
    private Map<Graphic2D, Pixi.DisplayObject> displayObjects;

    private Graphic2D currentGraphic;
    private Pixi.DisplayObject currentDisplayObject;
    private int zIndex;

    private static final Logger LOGGER = LogHelper.getLogger(PixiGraphics.class);

    public PixiGraphics(Canvas canvas) {
        this.canvas = canvas;
        this.displayObjects = new HashMap<>();
        this.pixi = Browser.getPixiInterface();
    }

    @Override
    public void init() {
        pixi.init();
        pixi.createLayer(Stage.DEFAULT_LAYER);
    }

    private Pixi.Rectangle getScreen() {
        return pixi.getPixiApp().getScreen();
    }

    @Override
    public int getDisplayWidth() {
        return Math.round(getScreen().getWidth());
    }

    @Override
    public int getDisplayHeight() {
        return Math.round(getScreen().getHeight());
    }

    @Override
    public float getDevicePixelRatio() {
        return 1f;
    }

    @Override
    public void preVisitStage(Stage stage) {
        currentGraphic = null;
        zIndex = 1;
    }

    @Override
    public void prepareLayer(Layer2D layer) {
        //TODO
//        if (layer.getShader() instanceof PixiShader pixiShader) {
//            pixiShader.compile(pixi);
//            pixi.applyFilter(layer.getName(), pixiShader.getFilter());
//        } else {
//            pixi.applyFilter(layer.getName(), null);
//        }
    }

    @Override
    public void preVisitGraphic(Graphic2D graphic, boolean visible) {
        currentGraphic = graphic;
        currentDisplayObject = displayObjects.get(currentGraphic);
        if (currentDisplayObject == null) {
            LOGGER.warning("No display object for " + graphic);
        }
        currentDisplayObject.setVisible(visible);
    }

    @Override
    public void drawBackground(ColorRGB color) {
        pixi.changeBackgroundColor(color.getRGB());
    }

    @Override
    public void drawSprite(Sprite sprite) {
        Pixi.DisplayObject[] containerContents = currentDisplayObject.getChildren();
        if (containerContents.length == 0) {
            // Sprite texture is still loading, try again next frame.
            return;
        }

        Pixi.DisplayObject spriteDisplayObject = containerContents[0];
        TeaImage image = getImage(sprite);
        Pixi.Texture texture = spriteDisplayObject.getTexture();
        Transform transform = sprite.getTransform();

        spriteDisplayObject.setX(toScreenX(sprite.getPosition()));
        spriteDisplayObject.setY(toScreenY(sprite.getPosition()));
        spriteDisplayObject.setAlpha(transform.getAlpha() / 100f);
        spriteDisplayObject.setAngle(transform.getRotation());
        spriteDisplayObject.getScale().setX(transform.getScaleX() / 100f);
        spriteDisplayObject.getScale().setY(transform.getScaleY() / 100f);

        updateTextureRegion(texture, image.getRegion());
        updateTint(sprite.getTransform().getMask());
    }

    private void updateTextureRegion(Pixi.Texture texture, Region region) {
        texture.getFrame().setX(region.x());
        texture.getFrame().setY(region.y());
        texture.getFrame().setWidth(region.width());
        texture.getFrame().setHeight(region.height());
        texture.updateUvs();
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
        float offset = -0.4f * text.getLineHeight();

        currentDisplayObject.setText(text.getText());
        currentDisplayObject.setX(toScreenX(text.getPosition()));
        currentDisplayObject.setY(toScreenY(text.getPosition().getY() + offset));
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
    public void onLayerAdded(Layer2D layer) {
        pixi.createLayer(layer.getName());
    }

    @Override
    public void onGraphicAdded(Layer2D layer, Graphic2D graphic) {
        zIndex++;
        Pixi.DisplayObject displayObject = createDisplayObject(graphic);
        displayObject.setZIndex(zIndex);
        pixi.addDisplayObject(layer.getName(), displayObject);
        displayObjects.put(graphic, displayObject);
    }

    private Pixi.DisplayObject createDisplayObject(Graphic2D graphic) {
        if (graphic instanceof Sprite sprite) {
            return createSpriteDisplayObject(sprite);
        } else if (graphic instanceof Primitive) {
            return pixi.createGraphics();
        } else if (graphic instanceof Text text) {
            return createTextDisplayObject(text);
        } else {
            throw new IllegalArgumentException("Unknown graphics type: " + graphic);
        }
    }

    private Pixi.DisplayObject createSpriteDisplayObject(Sprite sprite) {
        TeaImage image = getImage(sprite);
        Pixi.DisplayObject spriteContainer = pixi.createContainer();

        image.getImagePromise().then(img -> {
            Region region = image.getRegion();

            Pixi.DisplayObject spriteDisplayObject = pixi.createSprite(img,
                region.x(), region.y(), region.width(), region.height());

            spriteContainer.addChild(spriteDisplayObject);
        });

        return spriteContainer;
    }

    private Pixi.DisplayObject createTextDisplayObject(Text text) {
        FontStyle style = text.getFont().scale(canvas).getStyle();

        return pixi.createText(style.family(), style.size(), style.bold(),
            text.getAlign().toString(), text.getLineHeight(), style.color().getRGB());
    }

    private TeaImage getImage(Sprite sprite) {
        return (TeaImage) sprite.getCurrentGraphics();
    }

    @Override
    public void onGraphicRemoved(Layer2D layer, Graphic2D graphic) {
        Pixi.DisplayObject displayObject = displayObjects.get(graphic);
        if (displayObject != null) {
            pixi.removeDisplayObject(layer.getName(), displayObject);
        }
    }

    @Override
    public void onStageCleared() {
        pixi.clearStage();
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }
}
