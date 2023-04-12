//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Layer2D;
import nl.colorize.multimedialib.stage.Layer3D;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.StageVisitor;
import nl.colorize.multimedialib.stage.Text;
import nl.colorize.multimedialib.stage.Transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;

public class GDXGraphics implements StageVisitor {

    private Canvas canvas;

    private PerspectiveCamera camera;
    private DirectionalLight light;
    private Environment environment;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeBatch;
    private Map<TextureRegion, TextureRegion> maskCache;

    private ModelBatch modelBatch;

    private static final int FIELD_OF_VIEW = 75;
    private static final float NEAR_PLANE = 1f;
    private static final float FAR_PLANE = 300f;
    private static final Transform DEFAULT_TRANSFORM = new Transform();
    private static final int CIRCLE_SEGMENTS = 32;

    protected GDXGraphics(Canvas canvas) {
        this.canvas = canvas;

        this.spriteBatch = new SpriteBatch();
        this.shapeBatch = new ShapeRenderer();
        this.maskCache = new HashMap<>();

        this.modelBatch = new ModelBatch();

        prepareEnvironment();
    }

    private void prepareEnvironment() {
        camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = NEAR_PLANE;
        camera.far = FAR_PLANE;
        camera.update();

        light = new DirectionalLight();
        environment = new Environment();
        environment.add(light);
    }

    @Override
    public void prepareLayer(Layer2D layer) {
        //TODO
        //if (layer.getShader() instanceof GDXShader gdxShader) {
        //    spriteBatch.setShader(gdxShader.getShaderProgram());
        //} else {
        //    spriteBatch.setShader(null);
        //}
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        switchMode(false, true);
        shapeBatch.setColor(convertColor(backgroundColor, 100f));
        shapeBatch.rect(toScreenX(0f), toScreenY(0f),
            toScreenX(canvas.getWidth()), toScreenY(canvas.getHeight()));
    }

    /**
     * Draws a line using libGDX's {@code ShapeBatch}. Drawing lines will
     * always trigger a mode switch, as "line mode" and "fill mode" are
     * separate and lines are the only shape without a fill.
     */
    @Override
    public void drawLine(Primitive graphic, Line line) {
        float x0 = toScreenX(line.getStart().getX());
        float y0 = toScreenY(line.getStart().getY());
        float x1 = toScreenX(line.getEnd().getX());
        float y1 = toScreenY(line.getEnd().getY());

        switchMode(false, false);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeBatch.begin(ShapeRenderer.ShapeType.Line);
        shapeBatch.setColor(convertColor(graphic.getColor()));
        shapeBatch.line(x0, y0, x1, y1);
        shapeBatch.end();
    }

    @Override
    public void drawRect(Primitive graphic, Rect rect) {
        float x = toScreenX(rect.getX());
        float y = toScreenY(rect.getEndY());
        float width = rect.getWidth() * canvas.getZoomLevel();
        float height = rect.getHeight() * canvas.getZoomLevel();

        switchMode(false, true);
        shapeBatch.setColor(convertColor(graphic.getColor(), graphic.getAlpha()));
        shapeBatch.rect(x, y, width, height);
    }

    @Override
    public void drawCircle(Primitive graphic, Circle circle) {
        float x = toScreenX(circle.getCenterX());
        float y = toScreenY(circle.getCenterY());
        float radius = circle.getRadius() * canvas.getZoomLevel();

        switchMode(false, true);
        shapeBatch.setColor(convertColor(graphic.getColor(), graphic.getAlpha()));
        shapeBatch.circle(x, y, radius, CIRCLE_SEGMENTS);
    }

    @Override
    public void drawPolygon(Primitive graphic, Polygon polygon) {
        if (polygon.getNumPoints() == 3) {
            drawTriangle(polygon.getVertices(), graphic.getColor(), graphic.getAlpha());
        } else {
            for (Polygon triangle : polygon.subdivide()) {
                drawTriangle(triangle.getVertices(), graphic.getColor(), graphic.getAlpha());
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
    public void drawSprite(Sprite sprite) {
        TextureRegion textureRegion = ((GDXImage) sprite.getCurrentGraphics()).getTextureRegion();
        Transform transform = sprite.getTransform();
        if (transform == null) {
            transform = DEFAULT_TRANSFORM;
        }
        drawSprite(textureRegion, sprite.getPosition(), transform);
    }

    private void drawSprite(TextureRegion textureRegion, Point2D position, Transform transform) {
        float screenX = toScreenX(position.getX());
        float screenY = toScreenY(position.getY());
        float screenWidth = textureRegion.getRegionWidth() * canvas.getZoomLevel();
        float screenHeight = textureRegion.getRegionHeight() * canvas.getZoomLevel();

        if (transform.getMask() != null) {
            textureRegion = getMask(textureRegion, transform.getMask());
        }

        switchMode(true, false);
        spriteBatch.setColor(1f, 1f, 1f, transform.getAlpha() / 100f);
        spriteBatch.draw(textureRegion, screenX - screenWidth / 2f, screenY - screenHeight / 2f,
            screenWidth / 2f, screenHeight / 2f, screenWidth, screenHeight,
            transform.getScaleX() / 100f, transform.getScaleY() / 100f, -transform.getRotation());
    }

    private TextureRegion getMask(TextureRegion textureRegion, ColorRGB color) {
        TextureRegion mask = maskCache.get(textureRegion);
        if (mask == null) {
            mask = createMask(textureRegion, color);
            maskCache.put(textureRegion, mask);
        }
        return mask;
    }

    private TextureRegion createMask(TextureRegion original, ColorRGB color) {
        TextureData textureData = original.getTexture().getTextureData();
        textureData.prepare();
        Pixmap pixels = textureData.consumePixmap();

        Pixmap mask = new Pixmap(original.getRegionWidth(), original.getRegionHeight(), RGBA8888);

        for (int x = 0; x < original.getRegionWidth(); x++) {
            for (int y = 0; y < original.getRegionHeight(); y++) {
                int rgba = pixels.getPixel(original.getRegionX() + x, original.getRegionY() + y);
                int maskRGBA = Color.rgba8888(convertColor(color, new Color(rgba).a * 100f));
                mask.drawPixel(x, y, maskRGBA);
            }
        }

        Texture texture = new Texture(mask);

        pixels.dispose();
        mask.dispose();

        return new TextureRegion(texture);
    }

    @Override
    public void drawText(Text text) {
        GDXBitmapFont baseFont = (GDXBitmapFont) text.getFont();
        GDXBitmapFont displayFont = (GDXBitmapFont) baseFont.scale(canvas);
        float screenX = toScreenX(text.getPosition().getX());
        int align = getTextAlign(text.getAlign());

        switchMode(true, false);

        text.forLines((i, line) -> {
            float lineY = text.getPosition().getY() + i * text.getLineHeight();
            float screenY = toScreenY(lineY - 0.4f * displayFont.getStyle().size());
            displayFont.getBitmapFont().draw(spriteBatch, line, screenX, screenY, 0, align, false);
        });
    }

    private int getTextAlign(Align align) {
        return switch (align) {
            case LEFT -> com.badlogic.gdx.utils.Align.left;
            case CENTER -> com.badlogic.gdx.utils.Align.center;
            case RIGHT -> com.badlogic.gdx.utils.Align.right;
            default -> throw new AssertionError();
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

    public void render3D(Layer3D layer) {
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

    private List<ModelInstance> updateDisplayList(Layer3D layer) {
        return Streams.stream(layer.getModels())
            .map(model -> (GDXModel) model)
            .map(gdxModel -> gdxModel.getInstance())
            .toList();
    }

    private Vector3 toVector(Point3D point) {
        return new Vector3(point.getX(), point.getY(), point.getZ());
    }

    protected void dispose() {
        endSpriteBatch();
        endShapeBatch();

        spriteBatch.dispose();
        shapeBatch.dispose();
        modelBatch.dispose();
    }
}
