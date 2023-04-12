//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;

/**
 * Refers to a texture that is managed by libGDX. Each texture also keeps track
 * of all regions that have been defined within it.
 */
public class GDXImage implements Image {

    private FilePointer origin;
    private Texture texture;
    private TextureRegion textureRegion;
    private Pixmap textureData;
    private Region bounds;

    protected GDXImage(FilePointer origin, Texture texture, Region bounds) {
        float u1 = bounds.x() / (float) texture.getWidth();
        float v1 = bounds.y() / (float) texture.getHeight();
        float u2 = bounds.x1() / (float) texture.getWidth();
        float v2 = bounds.y1() / (float) texture.getHeight();

        this.origin = origin;
        this.texture = texture;
        this.textureRegion = new TextureRegion(texture, u1, v1, u2, v2);
        this.bounds = bounds;
    }

    protected GDXImage(FilePointer origin, Texture texture) {
        this(origin, texture, new Region(0, 0, texture.getWidth(), texture.getHeight()));
    }

    protected TextureRegion getTextureRegion() {
        return textureRegion;
    }

    @Override
    public Region getRegion() {
        return bounds;
    }

    @Override
    public Image extractRegion(Region region) {
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
}
