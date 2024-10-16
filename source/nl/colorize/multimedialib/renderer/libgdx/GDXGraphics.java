//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.multimedialib.stage.World3D;
import nl.colorize.util.stats.Cache;

import java.util.List;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.utils.Align.center;
import static com.badlogic.gdx.utils.Align.left;
import static com.badlogic.gdx.utils.Align.right;

public class GDXGraphics implements StageVisitor {

    private Canvas canvas;
    private GDXMediaLoader mediaLoader;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeBatch;
    private Cache<MaskTexture, TextureRegion> maskCache;

    private PerspectiveCamera camera;
    private DirectionalLight light;
    private Environment environment;
    private ModelBatch modelBatch;

    private static final int FIELD_OF_VIEW = 75;
    private static final float NEAR_PLANE = 1f;
    private static final float FAR_PLANE = 300f;
    private static final int CIRCLE_SEGMENTS = 32;
    private static final int MASK_CACHE_SIZE = 1024;

    protected GDXGraphics(Canvas canvas, GDXMediaLoader mediaLoader) {
        this.canvas = canvas;
        this.mediaLoader = mediaLoader;
        this.maskCache = Cache.from(this::createMask, MASK_CACHE_SIZE);

        prepareWorld();
        restartBatch();
    }

    private void prepareWorld() {
        camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = NEAR_PLANE;
        camera.far = FAR_PLANE;
        camera.update();

        light = new DirectionalLight();
        environment = new Environment();
        environment.add(light);
    }

    protected void restartBatch() {
        spriteBatch = new SpriteBatch();
        shapeBatch = new ShapeRenderer();
        modelBatch = new ModelBatch();
    }

    @Override
    public void prepareStage(Stage stage) {
    }

    @Override
    public boolean shouldVisitAllGraphics() {
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
            drawTriangle(polygon.points(), graphic.getColor(), globalTransform.getAlpha());
        } else {
            for (Polygon triangle : polygon.subdivide()) {
                drawTriangle(triangle.points(), graphic.getColor(), globalTransform.getAlpha());
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

    /**
     * Switches graphics modes. libGDX is heavily reliant on performing drawing
     * operations in batch mode. As a consequence, there is a performance
     * penalty when drawing sprites and shapes interchangeably, as this will
     * trigger several mode switches during each frame.
     */
    protected void switchMode(boolean sprites, boolean shapes) {
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

    public void render3D(World3D layer) {
        camera.position.set(toVector(layer.getCameraPosition()));
        camera.up.set(0f, 1f, 0f);
        camera.lookAt(toVector(layer.getCameraTarget()));
        camera.update();

        Color ambient = GDXMediaLoader.toColor(layer.getAmbientLight());
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambient));

        Color lightColor = GDXMediaLoader.toColor(layer.getLightColor());
        Vector3 lightPosition = toVector(layer.getLightPosition());
        light.set(lightColor, lightPosition);

        List<ModelInstance> displayList = updateDisplayList(layer);
        modelBatch.begin(camera);
        modelBatch.render(displayList, environment);
        modelBatch.end();
    }

    private List<ModelInstance> updateDisplayList(World3D world) {
        return Streams.stream(world.getChildren())
            .map(model -> (GDXModel) model)
            .map(gdxModel -> gdxModel.getInstance())
            .toList();
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
