//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Group;
import nl.colorize.multimedialib.stage.Light;
import nl.colorize.multimedialib.stage.Mesh;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageNode3D;
import nl.colorize.multimedialib.stage.StageSubscriber;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.Transform3D;
import nl.colorize.util.stats.Cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.utils.Align.center;
import static com.badlogic.gdx.utils.Align.left;
import static com.badlogic.gdx.utils.Align.right;

public class GDXGraphics implements StageVisitor, StageSubscriber {

    private GraphicsMode graphicsMode;
    private Canvas canvas;
    private GDXMediaLoader mediaLoader;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeBatch;
    private Cache<MaskTexture, TextureRegion> maskCache;

    protected PerspectiveCamera camera;
    private Environment environment;
    private Map<Light, PointLight> lights;
    private ModelBatch modelBatch;
    private List<ModelInstance> displayList;

    private static final int FIELD_OF_VIEW = 75;
    private static final float NEAR_PLANE = 1;
    private static final float FAR_PLANE = 300;
    private static final int CIRCLE_SEGMENTS = 32;
    private static final int MASK_CACHE_SIZE = 1024;

    protected GDXGraphics(GraphicsMode graphicsMode, Canvas canvas, GDXMediaLoader mediaLoader) {
        this.graphicsMode = graphicsMode;
        this.canvas = canvas;
        this.mediaLoader = mediaLoader;
        this.maskCache = Cache.from(this::createMask, MASK_CACHE_SIZE);

        camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = NEAR_PLANE;
        camera.far = FAR_PLANE;
        camera.update();

        environment = new Environment();
        lights = new HashMap<>();

        restartBatch();
    }

    protected void restartBatch() {
        spriteBatch = new SpriteBatch();
        shapeBatch = new ShapeRenderer();
        modelBatch = new ModelBatch();
        if (displayList != null) {
            displayList.clear();
        }
    }

    @Override
    public void prepareStage(Stage stage) {
        if (displayList == null) {
            displayList = new ArrayList<>();
            stage.subscribe(this);
        }

        if (graphicsMode == GraphicsMode.MODE_3D) {
            camera.position.set(toVector(stage.getCameraPosition()));
            camera.up.set(0f, 1f, 0f);
            camera.lookAt(toVector(stage.getCameraFocus()));
            camera.update();

            Color ambient = GDXMediaLoader.toColor(stage.getAmbientLightColor());
            environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambient));

            displayList.clear();
        }
    }

    @Override
    public void onNodeAdded(Group parent, StageNode3D node) {
        if (node instanceof Light light) {
            PointLight gdxLight = new PointLight();
            environment.add(gdxLight);
            lights.put(light, gdxLight);
        }
    }

    @Override
    public void onNodeRemoved(Group parent, StageNode3D node) {
        if (node instanceof Light light) {
            PointLight gdxLight = lights.get(light);
            environment.remove(gdxLight);
            lights.remove(light);
        }
    }

    @Override
    public boolean shouldVisitAllNodes() {
        return false;
    }

    @Override
    public void visitContainer(Container container, Transform globalTransform) {
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        switchMode(false, true);
        shapeBatch.setColor(convertColor(backgroundColor));
        shapeBatch.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        switchMode(false, false);
    }

    /**
     * Draws a line using libGDX's {@code ShapeBatch}. Drawing lines will
     * always trigger a mode switch, as "line mode" and "fill mode" are
     * separate and lines are the only shape without a fill.
     */
    @Override
    public void drawLine(Primitive graphic, Line line, Transform globalTransform) {
        if (graphic.getStroke() == 1f) {
            drawBasicLines(List.of(line), graphic.getColor());
        } else {
            drawComplexLines(List.of(line), graphic.getColor(), graphic.getStroke());
        }
    }

    @Override
    public void drawSegmentedLine(Primitive graphic, SegmentedLine line, Transform globalTransform) {
        if (graphic.getStroke() == 1f) {
            drawBasicLines(line.getSegments(), graphic.getColor());
        } else {
            drawComplexLines(line.getSegments(), graphic.getColor(), graphic.getStroke());
        }
    }

    private void drawBasicLines(List<Line> lines, ColorRGB color) {
        switchMode(false, false);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeBatch.begin(ShapeRenderer.ShapeType.Line);
        shapeBatch.setColor(convertColor(color));

        for (Line segment : lines) {
            float x0 = toScreenX(segment.start().x());
            float y0 = toScreenY(segment.start().y());
            float x1 = toScreenX(segment.end().x());
            float y1 = toScreenY(segment.end().y());

            shapeBatch.line(x0, y0, x1, y1);
        }

        shapeBatch.end();
    }

    private void drawComplexLines(List<Line> lines, ColorRGB color, float stroke) {
        switchMode(false, false);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        shapeBatch.begin(ShapeRenderer.ShapeType.Filled);
        shapeBatch.setColor(convertColor(color));

        for (Line segment : lines) {
            float x0 = toScreenX(segment.start().x());
            float y0 = toScreenY(segment.start().y());
            float x1 = toScreenX(segment.end().x());
            float y1 = toScreenY(segment.end().y());

            shapeBatch.rectLine(new Vector2(x0, y0), new Vector2(x1, y1), stroke);
        }

        shapeBatch.end();
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect, Transform globalTransform) {
        float x = toScreenX(rect.x());
        float y = toScreenY(rect.getEndY());
        float width = rect.width() * canvas.getZoomLevel();
        float height = rect.height() * canvas.getZoomLevel();

        switchMode(false, true);
        shapeBatch.setColor(convertColor(graphic.getColor(), globalTransform.getAlpha()));
        shapeBatch.rect(x, y, width, height);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle, Transform globalTransform) {
        float x = toScreenX(circle.center().x());
        float y = toScreenY(circle.center().y());
        float radius = circle.radius() * canvas.getZoomLevel();

        switchMode(false, true);
        shapeBatch.setColor(convertColor(graphic.getColor(), globalTransform.getAlpha()));
        shapeBatch.circle(x, y, radius, CIRCLE_SEGMENTS);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon, Transform globalTransform) {
        if (polygon.getNumPoints() == 3) {
            drawTriangle(polygon.toPoints(), graphic.getColor(), globalTransform.getAlpha());
        } else {
            for (Polygon triangle : polygon.subdivide()) {
                drawTriangle(triangle.toPoints(), graphic.getColor(), globalTransform.getAlpha());
            }
        }
    }

    private void drawTriangle(float[] vertices, ColorRGB color, float alpha) {
        switchMode(false, true);
        shapeBatch.setColor(convertColor(color, alpha));
        shapeBatch.triangle(toScreenX(vertices[0]), toScreenY(vertices[1]),
            toScreenX(vertices[2]), toScreenY(vertices[3]),
            toScreenX(vertices[4]), toScreenY(vertices[5]));
    }

    @Override
    public void drawSprite(Sprite sprite, Transform globalTransform) {
        TextureRegion textureRegion = ((GDXImage) sprite.getCurrentGraphics()).getTextureRegion();
        drawSprite(textureRegion, globalTransform);
    }

    private void drawSprite(TextureRegion textureRegion, Transform transform) {
        float screenX = toScreenX(transform.getPosition().x());
        float screenY = toScreenY(transform.getPosition().y());
        float screenWidth = textureRegion.getRegionWidth() * canvas.getZoomLevel();
        float screenHeight = textureRegion.getRegionHeight() * canvas.getZoomLevel();

        if (transform.getMaskColor() != null) {
            textureRegion = maskCache.get(new MaskTexture(textureRegion, transform.getMaskColor()));
        }

        switchMode(true, false);
        spriteBatch.setColor(1f, 1f, 1f, transform.getAlpha() / 100f);
        spriteBatch.draw(textureRegion, screenX - screenWidth / 2f, screenY - screenHeight / 2f,
            screenWidth / 2f, screenHeight / 2f, screenWidth, screenHeight,
            transform.getScaleX() / 100f, transform.getScaleY() / 100f,
            -transform.getRotation().degrees());
    }

    private TextureRegion createMask(MaskTexture config) {
        TextureRegion original = config.original;
        TextureData textureData = original.getTexture().getTextureData();
        textureData.prepare();
        Pixmap pixels = textureData.consumePixmap();

        Pixmap mask = new Pixmap(original.getRegionWidth(), original.getRegionHeight(), RGBA8888);

        for (int x = 0; x < original.getRegionWidth(); x++) {
            for (int y = 0; y < original.getRegionHeight(); y++) {
                int rgba = pixels.getPixel(original.getRegionX() + x, original.getRegionY() + y);
                int maskRGBA = Color.rgba8888(convertColor(config.color, new Color(rgba).a * 100f));
                mask.drawPixel(x, y, maskRGBA);
            }
        }

        Texture texture = new Texture(mask);

        pixels.dispose();
        mask.dispose();

        return new TextureRegion(texture);
    }

    @Override
    public void drawText(Text text, Transform globalTransform) {
        FontFace scaledFont = text.getFont().scale(canvas);
        BitmapFont bitmapFont = mediaLoader.getBitmapFont(scaledFont);
        float screenX = toScreenX(globalTransform.getPosition().x());
        int align = getTextAlign(text.getAlign());
        // We cannot use the font metrics reported by the BitmapFont
        // itself, since those numbers don't work well with scaling.
        float ascent = 0.8f * text.getFont().size();

        switchMode(true, false);

        text.forLines((i, line) -> {
            float lineY = globalTransform.getPosition().y() + i * text.getLineHeight() - ascent;
            float screenY = toScreenY(lineY);
            bitmapFont.draw(spriteBatch, line, screenX, screenY, 0, align, false);
        });
    }

    private int getTextAlign(Align align) {
        return switch (align) {
            case LEFT -> left;
            case CENTER -> center;
            case RIGHT -> right;
        };
    }

    @Override
    public void finalize2D(Stage stage) {
        switchMode(false, false);
    }

    @Override
    public void visitGroup(Group group, Transform3D globalTransform) {
    }

    @Override
    public void drawMesh(Mesh mesh, Transform3D globalTransform) {
        if (globalTransform.isVisible()) {
            GDXModel gdxModel = (GDXModel) mesh;
            syncTransform(gdxModel.getModelInstance(), globalTransform);
            displayList.add(gdxModel.getModelInstance());
        }
    }

    private void syncTransform(ModelInstance modelInstance, Transform3D globalTransform) {
        Vector3 positionVector = convertVector(globalTransform.getPosition());
        modelInstance.transform.setToTranslation(positionVector);

        modelInstance.transform.rotate(1f, 0f, 0f, globalTransform.getRotationX().degrees());
        modelInstance.transform.rotate(0f, 1f, 0f, globalTransform.getRotationY().degrees());
        modelInstance.transform.rotate(0f, 0f, 1f, globalTransform.getRotationZ().degrees());

        modelInstance.transform.scale(
            globalTransform.getScaleX() / 100f,
            globalTransform.getScaleY() / 100f,
            globalTransform.getScaleZ() / 100f
        );
    }

    @Override
    public void drawLight(Light light, Transform3D globalTransform) {
        // Unlike models, libGDX does actually keep track
        // of all lights in its environment. So we only
        // need to update the light's state.
        PointLight gdxLight = lights.get(light);
        gdxLight.setColor(convertColor(light.getColor()));
        gdxLight.setPosition(convertVector(globalTransform.getPosition()));
        gdxLight.setIntensity(globalTransform.isVisible() ? light.getIntensity() : 0f);
    }

    @Override
    public void finalize3D(Stage stage) {
        if (graphicsMode == GraphicsMode.MODE_3D) {
            modelBatch.begin(camera);
            modelBatch.render(displayList, environment);
            modelBatch.end();
        }
    }

    private float toScreenX(float x) {
        return canvas.toScreenX(x);
    }

    public float toScreenY(float y) {
        return Gdx.graphics.getHeight() - canvas.toScreenY(y);
    }

    private Color convertColor(ColorRGB color, float alpha) {
        return new Color(color.r() / 255f, color.g() / 255f, color.b() / 255f, alpha / 100f);
    }

    private Color convertColor(ColorRGB color) {
        return convertColor(color, 100f);
    }

    private Vector3 convertVector(Point3D point) {
        return new Vector3(point.x(), point.y(), point.z());
    }

    /**
     * Switches graphics modes. libGDX is heavily reliant on performing drawing
     * operations in batch mode. As a consequence, there is a performance
     * penalty when drawing sprites and shapes interchangeably, as this will
     * trigger several mode switches during each frame.
     */
    private void switchMode(boolean sprites, boolean shapes) {
        Preconditions.checkArgument(!(sprites && shapes), "Invalid drawing mode");

        if (sprites) {
            endShapeBatch();
            beginSpriteBatch();
        } else if (shapes) {
            endSpriteBatch();
            beginShapeBatch();
        } else {
            endSpriteBatch();
            endShapeBatch();
        }
    }

    private void beginSpriteBatch() {
        if (!spriteBatch.isDrawing()) {
            spriteBatch.begin();
        }
    }

    private void endSpriteBatch() {
        if (spriteBatch.isDrawing()) {
            spriteBatch.end();
        }
    }

    private void beginShapeBatch() {
        if (!shapeBatch.isDrawing()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeBatch.begin(ShapeRenderer.ShapeType.Filled);
        }
    }

    private void endShapeBatch() {
        if (shapeBatch.isDrawing()) {
            shapeBatch.end();
        }
    }

    private Vector3 toVector(Point3D point) {
        return new Vector3(point.x(), point.y(), point.z());
    }

    protected void dispose() {
        endSpriteBatch();
        endShapeBatch();

        spriteBatch.dispose();
        shapeBatch.dispose();
        modelBatch.dispose();
    }

    /**
     * Masks the original texture or texture region by replacing every
     * non-transparent pixel with a mask color.
     */
    private record MaskTexture(TextureRegion original, ColorRGB color) {
    }
}
