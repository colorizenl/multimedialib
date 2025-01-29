//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.teavm.PixiBridge.PixiDisplayObject;
import nl.colorize.multimedialib.renderer.teavm.PixiBridge.PixiRectangle;
import nl.colorize.multimedialib.renderer.teavm.PixiBridge.PixiTexture;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Light;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageNode2D;
import nl.colorize.multimedialib.stage.StageSubscriber;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.TextUtils;
import nl.colorize.util.stats.Cache;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLImageElement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Uses the <a href="https://pixijs.com">PixiJS</a> JavaScript library to
 * render 2D graphics. PixiJS will use either a WebGL-based renderer or a
 * canvas-based renderer, depending on the browser and platform.
 */
public class PixiGraphics implements TeaGraphics, StageSubscriber {

    private Canvas canvas;
    private TeaMediaLoader mediaLoader;
    private PixiBridge pixi;
    private Map<StageNode2D, PixiDisplayObject> displayObjects;
    private Cache<TeaImage, PixiTexture> textureCache;

    private static final int TEXTURE_CACHE_SIZE = 2048;

    public PixiGraphics() {
        this.displayObjects = new HashMap<>();
        this.textureCache = Cache.from(this::prepareTexture, TEXTURE_CACHE_SIZE);
    }

    @Override
    public void init(SceneContext context) {
        this.canvas = context.getCanvas();
        this.mediaLoader = (TeaMediaLoader) context.getMediaLoader();
        this.pixi = Browser.getPixiBridge();
        pixi.init();

        Container rootContainer = context.getStage().getRoot();
        displayObjects.put(rootContainer, pixi.getRootContainer());
    }

    private PixiRectangle getScreen() {
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
    public void onNodeAdded(Container parent, StageNode2D node) {
        PixiDisplayObject parentDisplayObject = displayObjects.get(parent);
        PixiDisplayObject displayObject = createDisplayObject(node);
        parentDisplayObject.addChild(displayObject);
        displayObjects.put(node, displayObject);
    }

    @Override
    public void onNodeRemoved(Container parent, StageNode2D node) {
        PixiDisplayObject parentDisplayObject = displayObjects.get(parent);
        PixiDisplayObject displayObject = getDisplayObject(node);
        parentDisplayObject.removeChild(displayObject);
        displayObjects.remove(node);
    }

    @Override
    public void prepareStage(Stage stage) {
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
        PixiDisplayObject displayObject = displayObjects.get(container);
        displayObject.setVisible(container.getTransform().isVisible());
    }

    @Override
    public boolean shouldVisitAllNodes() {
        return true;
    }

    private PixiDisplayObject createDisplayObject(StageNode2D graphic) {
        PixiDisplayObject existing = displayObjects.get(graphic);

        if (existing != null) {
            return existing;
        }

        return switch (graphic) {
            case Container container -> pixi.createContainer();
            case Sprite sprite -> createSpriteDisplayObject(sprite);
            case Primitive primitive -> pixi.createGraphics();
            case Text text -> createTextDisplayObject(text);
            default -> throw new RendererException("Unknown graphics type: " + graphic);
        };
    }

    private PixiDisplayObject createSpriteDisplayObject(Sprite sprite) {
        TeaImage image = getImage(sprite);
        PixiDisplayObject spriteContainer = pixi.createContainer();

        image.getImagePromise().subscribe(imageElement -> {
            PixiTexture texture = textureCache.get(image);
            PixiDisplayObject spriteDisplayObject = pixi.createSprite(texture);
            spriteContainer.addChild(spriteDisplayObject);
        });

        return spriteContainer;
    }

    private PixiDisplayObject createTextDisplayObject(Text text) {
        FontFace scaledFont = text.getFont().scale(canvas);
        return pixi.createText(scaledFont.family(), scaledFont.size(), false,
            text.getAlign().toString(), text.getLineHeight(), scaledFont.color().getRGB());
    }

    private TeaImage getImage(Sprite sprite) {
        return (TeaImage) sprite.getCurrentGraphics();
    }

    private PixiTexture prepareTexture(TeaImage image) {
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

    private PixiDisplayObject getDisplayObject(StageNode2D graphic) {
        PixiDisplayObject displayObject = displayObjects.get(graphic);
        if (displayObject == null) {
            throw new RendererException("Creating unexpected display object for " + graphic);
        }
        displayObject.setVisible(graphic.getTransform().isVisible());
        return displayObject;
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        PixiDisplayObject displayObject = getDisplayObject(sprite);
        PixiDisplayObject[] containerContents = displayObject.getChildren();

        if (containerContents.length == 0) {
            // Sprite texture is still loading, try again next frame.
            return;
        }

        PixiDisplayObject spriteDisplayObject = containerContents[0];
        updateSprite(sprite, globalTransform, spriteDisplayObject);
    }

    private void updateSprite(Sprite sprite, Transform transform, PixiDisplayObject displayObject) {
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

    private void updateTexture(PixiDisplayObject sprite, TeaImage image) {
        PixiTexture texture = sprite.getTexture();

        if (!image.getId().toString().equals(texture.getTextureImageId())) {
            PixiTexture newTexture = textureCache.get(image);
            sprite.setTexture(newTexture);
        }
    }

    private void updateTextureRegion(PixiDisplayObject sprite, Region region) {
        PixiTexture texture = sprite.getTexture();
        PixiRectangle frame = texture.getFrame();

        frame.setX(region.x());
        frame.setY(region.y());
        frame.setWidth(region.width());
        frame.setHeight(region.height());
        texture.updateUvs();
    }

    private void updateMask(PixiDisplayObject sprite, TeaImage image, ColorRGB mask) {
        if (!sprite.isTintEnabled() && mask != null) {
            HTMLCanvasElement maskImage = mediaLoader.applyMask(image, mask);
            Region region = image.getRegion();
            UUID maskImageId = UUID.randomUUID();

            PixiTexture maskTexture = pixi.createTexture(maskImageId.toString(), maskImage,
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
        PixiDisplayObject displayObject = getDisplayObject(graphic);

        displayObject.clear();
        displayObject.lineStyle(Math.round(graphic.getStroke()), graphic.getColor().getRGB());
        displayObject.moveTo(toScreenX(line.start()), toScreenY(line.start()));
        displayObject.lineTo(toScreenX(line.end()), toScreenY(line.end()));
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        PixiDisplayObject displayObject = getDisplayObject(graphic);

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
        PixiDisplayObject displayObject = getDisplayObject(graphic);

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
        PixiDisplayObject displayObject = getDisplayObject(graphic);

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
        PixiDisplayObject displayObject = getDisplayObject(graphic);

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
        PixiDisplayObject displayObject = getDisplayObject(text);
        float offset = -0.65f * text.getLineHeight();

        displayObject.setText(TextUtils.LINE_JOINER.join(text.getLines()));
        displayObject.setX(toScreenX(globalTransform.getPosition()));
        displayObject.setY(toScreenY(globalTransform.getPosition().y() + offset));
        displayObject.setAlpha(globalTransform.getAlpha() / 100f);
    }

    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawLight(Light light, Transform3D globalTransform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point2D project(Point3D position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        throw new UnsupportedOperationException();
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
