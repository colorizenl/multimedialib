//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.FilePointer;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;

/**
 * Refers to a texture that is managed by libGDX. Each texture also keeps track
 * of all regions that have been defined within it.
 */
public class GDXImage implements Image {

    private FilePointer origin;
    private Texture texture;
    private TextureRegion textureRegion;
    private Pixmap textureData;
    private Rect bounds;

    protected GDXImage(FilePointer origin, Texture texture, Rect bounds) {
        float u1 = bounds.getX() / texture.getWidth();
        float v1 = bounds.getY() / texture.getHeight();
        float u2 = bounds.getEndX() / texture.getWidth();
        float v2 = bounds.getEndY() / texture.getHeight();

        this.origin = origin;
        this.texture = texture;
        this.textureRegion = new TextureRegion(texture, u1, v1, u2, v2);
        this.bounds = bounds;
    }

    protected GDXImage(FilePointer origin, Texture texture) {
        this(origin, texture, new Rect(0, 0, texture.getWidth(), texture.getHeight()));
    }

    public Texture getTexture() {
        return texture;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    @Override
    public Rect getRegion() {
        return bounds;
    }

    @Override
    public Image extractRegion(Rect region) {
        return new GDXImage(origin, texture, region);
    }

    private void loadTextureData() {
        if (textureData == null) {
            TextureData data = texture.getTextureData();
            data.prepare();
            textureData = data.consumePixmap();
        }
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        loadTextureData();
        int rgba = textureData.getPixel(x, y);
        return new ColorRGB(rgba);
    }

    @Override
    public int getAlpha(int x, int y) {
        loadTextureData();
        int rgba = textureData.getPixel(x, y);
        int alpha = (rgba >> 24) & 0xFF;
        return Math.round(alpha / 2.55f);
    }

    @Override
    public Image tint(ColorRGB color) {
        TextureData textureData = texture.getTextureData();
        textureData.prepare();

        Pixmap original = textureData.consumePixmap();
        Pixmap tinted = new Pixmap(getWidth(), getHeight(), RGBA8888);

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int rgba = original.getPixel(Math.round(bounds.getX()) + x, Math.round(bounds.getY()) + y);
                int maskRGBA = Color.rgba8888(color.getR() / 255f, color.getG() / 2f,
                    color.getB() / 255f, new Color(rgba).a * 100f);
                tinted.drawPixel(x, y, maskRGBA);
            }
        }

        Texture texture = new Texture(tinted);

        original.dispose();
        tinted.dispose();

        return new GDXImage(origin, texture);
    }

    @Override
    public String toString() {
        return origin + "@" + bounds;
    }
}
