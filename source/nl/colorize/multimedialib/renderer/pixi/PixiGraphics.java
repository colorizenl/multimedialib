//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.pixi;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.TeaGraphics;
import nl.colorize.multimedialib.renderer.teavm.TeaImage;
import nl.colorize.multimedialib.renderer.teavm.TeaMediaLoader;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Graphic2D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.LogHelper;
import nl.colorize.util.TextUtils;
import nl.colorize.util.stats.Cache;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Renders graphics using the <a href="https://pixijs.com">PixiJS</a> JavaScript
 * library. Depending on the platform and browser, PixiJS will either use WebGL
 * or fall back to the HTML canvas API.
 */
public class PixiGraphics implements TeaGraphics {

    private Canvas canvas;
    private TeaMediaLoader mediaLoader;
    private PixiBridge pixi;
    private Map<Graphic2D, Pixi.DisplayObject> displayObjects;
    private Cache<TeaImage, Pixi.Texture> textureCache;

    private static final int TEXTURE_CACHE_SIZE = 2048;
    private static final Logger LOGGER = LogHelper.getLogger(PixiGraphics.class);

    public PixiGraphics(Canvas canvas) {
        this.canvas = canvas;
        this.pixi = Browser.getPixiBridge();
        this.displayObjects = new HashMap<>();
        this.textureCache = Cache.from(this::prepareTexture, TEXTURE_CACHE_SIZE);
    }

    @Override
    public void init(TeaMediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
        pixi.init();
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
    public void prepareStage(Stage stage) {
        Container rootContainer = stage.getRoot();
        displayObjects.putIfAbsent(rootContainer, pixi.getRootContainer());
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
        Pixi.DisplayObject parent = displayObjects.get(container);
        parent.setVisible(container.getTransform().isVisible());

        for (Graphic2D added : container.getAddedChildren().flush()) {
            onGraphicAdded(parent, added);
        }

        for (Graphic2D removed : container.getRemovedChildren().flush()) {
            onGraphicRemoved(parent, removed);
        }
    }

    private void onGraphicAdded(Pixi.DisplayObject parent, Graphic2D added) {
        if (!displayObjects.containsKey(added)) {
            Pixi.DisplayObject displayObject = createDisplayObject(added);
            parent.addChild(displayObject);
            displayObjects.put(added, displayObject);
        }
    }

    private void onGraphicRemoved(Pixi.DisplayObject parent, Graphic2D removed) {
        if (displayObjects.containsKey(removed)) {
            parent.removeChild(getDisplayObject(removed));
            displayObjects.remove(removed);
        }
    }

    @Override
    public boolean shouldVisitAllGraphics() {
        return true;
    }

    private Pixi.DisplayObject createDisplayObject(Graphic2D graphic) {
        return switch (graphic) {
            case Container container -> pixi.createContainer();
            case Sprite sprite -> createSpriteDisplayObject(sprite);
            case Primitive primitive -> pixi.createGraphics();
            case Text text -> createTextDisplayObject(text);
            default -> throw new RendererException("Unknown graphics type: " + graphic);
        };
    }

    private Pixi.DisplayObject createSpriteDisplayObject(Sprite sprite) {
        TeaImage image = getImage(sprite);
        Pixi.DisplayObject spriteContainer = pixi.createContainer();

        image.getImagePromise().subscribe(imageElement -> {
            Pixi.Texture texture = textureCache.get(image);
            Pixi.DisplayObject spriteDisplayObject = pixi.createSprite(texture);
            spriteContainer.addChild(spriteDisplayObject);
        });

        return spriteContainer;
    }

    private Pixi.DisplayObject createTextDisplayObject(Text text) {
        FontFace scaledFont = text.getFont().scale(canvas);
        return pixi.createText(scaledFont.family(), scaledFont.size(), false,
            text.getAlign().toString(), text.getLineHeight(), scaledFont.color().getRGB());
    }

    private TeaImage getImage(Sprite sprite) {
        return (TeaImage) sprite.getCurrentGraphics();
    }

    private Pixi.Texture prepareTexture(TeaImage image) {
        Preconditions.checkState(image.isLoaded(), "Image is still loading");

        HTMLImageElement imgElement = image.getImageElement().get();
        Region region = image.getRegion();

        return pixi.createTexture(image.getId().toString(), imgElement,
            region.x(), region.y(), region.width(), region.height());
    }

    @Override
    public void drawBackground(ColorRGB color) {
        pixi.changeBackgroundColor(color.getRGB());
    }

    private Pixi.DisplayObject getDisplayObject(Graphic2D graphic) {
        Pixi.DisplayObject displayObject = displayObjects.get(graphic);
        if (displayObject == null) {
            throw new RendererException("Creating unexpected display object for " + graphic);
        }
        displayObject.setVisible(graphic.getTransform().isVisible());
        return displayObject;
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(sprite);
        Pixi.DisplayObject[] containerContents = displayObject.getChildren();

        if (containerContents.length == 0) {
            // Sprite texture is still loading, try again next frame.
            return;
        }

        Pixi.DisplayObject spriteDisplayObject = containerContents[0];
        updateSprite(sprite, globalTransform, spriteDisplayObject);
    }

    private void updateSprite(Sprite sprite, Transform transform, Pixi.DisplayObject displayObject) {
        TeaImage image = getImage(sprite);
        float zoom = canvas.getZoomLevel();

        displayObject.setX(toScreenX(transform.getPosition()));
        displayObject.setY(toScreenY(transform.getPosition()));
        displayObject.setAlpha(transform.getAlpha() / 100f);
        displayObject.setAngle(transform.getRotation().degrees());
        displayObject.getScale().setX((transform.getScaleX() * zoom) / 100f);
        displayObject.getScale().setY((transform.getScaleY() * zoom) / 100f);

        if (!displayObject.isTintEnabled()) {
            updateTexture(displayObject, image);
        }
        updateTextureRegion(displayObject, image.getRegion());
        updateMask(displayObject, image, transform.getMaskColor());
    }

    private void updateTexture(Pixi.DisplayObject sprite, TeaImage image) {
        Pixi.Texture texture = sprite.getTexture();

        if (!image.getId().toString().equals(texture.getTextureImageId())) {
            Pixi.Texture newTexture = textureCache.get(image);
            sprite.setTexture(newTexture);
        }
    }

    private void updateTextureRegion(Pixi.DisplayObject sprite, Region region) {
        Pixi.Texture texture = sprite.getTexture();
        Pixi.Rectangle frame = texture.getFrame();

        frame.setX(region.x());
        frame.setY(region.y());
        frame.setWidth(region.width());
        frame.setHeight(region.height());
        texture.updateUvs();
    }

    private void updateMask(Pixi.DisplayObject sprite, TeaImage image, ColorRGB mask) {
        if (!sprite.isTintEnabled() && mask != null) {
            HTMLCanvasElement maskImage = mediaLoader.applyMask(image, mask);
            Region region = image.getRegion();
            UUID maskImageId = UUID.randomUUID();

            Pixi.Texture maskTexture = pixi.createTexture(maskImageId.toString(), maskImage,
                region.x(), region.y(), region.width(), region.height());

            sprite.setTintEnabled(true);
            sprite.setOriginalTexture(sprite.getTexture());
            sprite.setTexture(maskTexture);
        } else if (sprite.isTintEnabled() && mask == null) {
            sprite.setTintEnabled(false);
            sprite.setTexture(sprite.getOriginalTexture());
        }
    }

    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(graphic);

        displayObject.clear();
        displayObject.lineStyle(Math.round(graphic.getStroke()), graphic.getColor().getRGB());
        displayObject.moveTo(toScreenX(line.start()), toScreenY(line.start()));
        displayObject.lineTo(toScreenX(line.end()), toScreenY(line.end()));
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(graphic);

        displayObject.clear();
        displayObject.lineStyle(Math.round(graphic.getStroke()), graphic.getColor().getRGB());
        displayObject.moveTo(toScreenX(line.getHead()), toScreenY(line.getHead()));
        for (Point2D p : line.points()) {
            displayObject.lineTo(toScreenX(p), toScreenY(p));
        }
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(graphic);

        displayObject.clear();
        displayObject.beginFill(graphic.getColor().getRGB(), 1f);
        displayObject.drawRect(
            toScreenX(rect.x()),
            toScreenY(rect.y()),
            rect.width() * canvas.getZoomLevel(),
            rect.height() * canvas.getZoomLevel()
        );
        displayObject.endFill();
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(graphic);

        displayObject.clear();
        displayObject.beginFill(graphic.getColor().getRGB(), 1f);
        displayObject.drawCircle(
            toScreenX(circle.center().x()),
            toScreenY(circle.center().y()),
            circle.radius() * canvas.getZoomLevel()
        );
        displayObject.endFill();
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(graphic);

        float[] points = new float[polygon.getNumPoints() * 2];
        for (int i = 0; i < polygon.getNumPoints(); i++) {
            points[i * 2] = toScreenX(polygon.getPointX(i));
            points[i * 2 + 1] = toScreenY(polygon.getPointY(i));
        }

        displayObject.clear();
        displayObject.beginFill(graphic.getColor().getRGB(), 1f);
        displayObject.drawPolygon(points);
        displayObject.endFill();

        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        Pixi.DisplayObject displayObject = getDisplayObject(text);
        float offset = -0.65f * text.getLineHeight();

        displayObject.setText(TextUtils.LINE_JOINER.join(text.getLines()));
        displayObject.setX(toScreenX(globalTransform.getPosition()));
        displayObject.setY(toScreenY(globalTransform.getPosition().y() + offset));
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    private float toScreenX(float x) {
        return canvas.toScreenX(x);
    }

    private float toScreenX(Point2D point) {
        return canvas.toScreenX(point.x());
    }

    private float toScreenY(float y) {
        return canvas.toScreenY(y);
    }

    private float toScreenY(Point2D point) {
        return canvas.toScreenY(point.y());
    }

    @Override
    public GraphicsMode getGraphicsMode() {
        return GraphicsMode.MODE_2D;
    }
}
