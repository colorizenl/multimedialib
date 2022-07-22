//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.ColorRGB;
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
import nl.colorize.multimedialib.scene.StageVisitor;

import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;

public class GDXGraphics2D implements StageVisitor {

    private Canvas canvas;
    private GDXMediaLoader mediaLoader;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeBatch;
    private Map<TextureRegion, TextureRegion> maskCache;

    private static final Transform DEFAULT_TRANSFORM = new Transform();
    private static final int CIRCLE_SEGMENTS = 32;

    protected GDXGraphics2D(Canvas canvas, GDXMediaLoader mediaLoader) {
        this.canvas = canvas;
        this.mediaLoader = mediaLoader;

        spriteBatch = new SpriteBatch();
        shapeBatch = new ShapeRenderer();
        maskCache = new HashMap<>();
    }

    @Override
    public void drawBackground(ColorRGB backgroundColor) {
        switchMode(false, true);
        shapeBatch.setColor(convertColor(backgroundColor, 100f));
        shapeBatch.rect(toScreenX(0f), toScreenY(0f),
            toScreenX(canvas.getWidth()), toScreenY(canvas.getHeight()));
    }

    @Override
    public void drawLine(Primitive graphic, Line line) {
        float x0 = toScreenX(line.getStart().getX());
        float y0 = toScreenY(line.getStart().getY());
        float x1 = toScreenX(line.getEnd().getX());
        float y1 = toScreenY(line.getEnd().getY());

        switchMode(false, true);
        shapeBatch.setColor(convertColor(graphic.getColor()));
        shapeBatch.line(x0, y0, x1, y1);
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
            transform.getScaleX() / 100f, transform.getScaleY() / 100f, transform.getRotation());
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
        TTFont font = text.getFont();
        int actualSize = Math.round(font.size() * canvas.getZoomLevel());
        BitmapFont bitmapFont = mediaLoader.getBitmapFont(font, actualSize);
        float screenX = toScreenX(text.getPosition().getX());
        int align = getTextAlign(text.getAlign());

        switchMode(true, false);

        text.forLines((i, line) -> {
            float lineY = text.getPosition().getY() + i * font.getLineHeight();
            float screenY = toScreenY(lineY - 0.4f * actualSize);
            bitmapFont.draw(spriteBatch, line, screenX, screenY, 0, align, false);
        });
    }

    private int getTextAlign(Align align) {
        switch (align) {
            case LEFT : return com.badlogic.gdx.utils.Align.left;
            case CENTER : return com.badlogic.gdx.utils.Align.center;
            case RIGHT : return com.badlogic.gdx.utils.Align.right;
            default : throw new AssertionError();
        }
    }

    private float toScreenX(float x) {
        return canvas.toScreenX(x);
    }

    public float toScreenY(float y) {
        return Gdx.graphics.getHeight() - canvas.toScreenY(y);
    }

    private Color convertColor(ColorRGB color, float alpha) {
        return new Color(color.getR() / 255f, color.getG() / 255f, color.getB() / 255f, alpha / 100f);
    }

    private Color convertColor(ColorRGB color) {
        return convertColor(color, 100f);
    }

    /**
     * Switches graphics modes. libGDX is heavily heavily reliant on performing
     * drawing operations in batch mode. As a consequence, there is a performance
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

    protected void dispose() {
        endSpriteBatch();
        endShapeBatch();
        spriteBatch.dispose();
        shapeBatch.dispose();
    }
}
