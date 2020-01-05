//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

/**
 * Refers to a texture that is managed by libGDX. Each texture also keeps track
 * of all regions that have been defined within it.
 */
public class GDXTexture implements Image {

    private Texture texture;
    private TextureRegion textureRegion;
    private Pixmap textureData;
    private Rect bounds;
    private boolean disposed;

    protected GDXTexture(Texture texture, Rect bounds) {
        float u1 = bounds.getX() / texture.getWidth();
        float v1 = bounds.getY() / texture.getHeight();
        float u2 = bounds.getEndX() / texture.getWidth();
        float v2 = bounds.getEndY() / texture.getHeight();

        this.texture = texture;
        this.textureRegion = new TextureRegion(texture, u1, v1, u2, v2);
        this.bounds = bounds;
        this.disposed = false;
    }

    protected GDXTexture(Texture texture) {
        this(texture, new Rect(0, 0, texture.getWidth(), texture.getHeight()));
    }

    public Texture getTexture() {
        Preconditions.checkState(!disposed, "Texture has already been disposed");
        return texture;
    }

    public TextureRegion getTextureRegion() {
        Preconditions.checkState(!disposed, "Texture has already been disposed");
        return textureRegion;
    }

    @Override
    public int getWidth() {
        return Math.round(bounds.getWidth());
    }

    @Override
    public int getHeight() {
        return Math.round(bounds.getHeight());
    }

    @Override
    public Image getRegion(Rect region) {
        return new GDXTexture(texture, region);
    }

    private void loadTextureData() {
        if (textureData == null) {
            textureData = texture.getTextureData().consumePixmap();
        }
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        Preconditions.checkArgument(x >= 0 && x < getWidth() && y >= 0 && y < getHeight(),
            "Invalid coordinate: " + x + ", " + y);

        loadTextureData();
        int rgba = textureData.getPixel(x, y);
        return new ColorRGB(rgba);
    }

    @Override
    public int getAlpha(int x, int y) {
        Preconditions.checkArgument(x >= 0 && x < getWidth() && y >= 0 && y < getHeight(),
            "Invalid coordinate: " + x + ", " + y);

        loadTextureData();
        int rgba = textureData.getPixel(x, y);
        int alpha = (rgba >> 24) & 0xFF;
        return Math.round(alpha / 2.55f);
    }

    public void dispose() {
        texture.dispose();
        disposed = true;
    }
}
