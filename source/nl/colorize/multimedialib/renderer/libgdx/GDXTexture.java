//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

/**
 * Refers to a texture that is managed by libGDX. Each texture also keeps track
 * of all regions that have been defined within it.
 */
public class GDXTexture implements Image {

    private Texture texture;
    private TextureRegion textureRegion;
    private Rect bounds;
    private boolean disposed;

    protected GDXTexture(Texture texture, Rect bounds) {
        this.texture = texture;
        this.textureRegion = new TextureRegion(texture, bounds.getX(), bounds.getY(),
            bounds.getWidth(), bounds.getHeight());
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
        return bounds.getWidth();
    }

    @Override
    public int getHeight() {
        return bounds.getHeight();
    }

    @Override
    public Image getRegion(Rect region) {
        return new GDXTexture(texture, region);
    }

    public void dispose() {
        texture.dispose();
        disposed = true;
    }
}
