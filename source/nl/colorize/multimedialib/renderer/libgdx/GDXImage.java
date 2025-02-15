//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;

/**
 * Refers to a texture that is managed by libGDX. Each texture also keeps track
 * of all regions that have been defined within it.
 */
@Getter
public class GDXImage implements Image {

    private Texture texture;
    private TextureRegion textureRegion;
    private Pixmap textureData;
    private Region region;

    public GDXImage(Texture texture, Region region) {
        float u0 = region.x() / (float) texture.getWidth();
        float v0 = region.y() / (float) texture.getHeight();
        float u1 = region.x1() / (float) texture.getWidth();
        float v1 = region.y1() / (float) texture.getHeight();

        this.texture = texture;
        this.textureRegion = new TextureRegion(texture, u0, v0, u1, v1);
        this.region = region;
    }

    public GDXImage(Texture texture) {
        this(texture, new Region(0, 0, texture.getWidth(), texture.getHeight()));
    }

    @Override
    public Image extractRegion(Region subRegion) {
        Region absoluteSubRegion = subRegion.move(region.x(), region.y());
        return new GDXImage(texture, absoluteSubRegion);
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
        int rgba = textureData.getPixel(region.x() + x, region.y() + y);
        return GDXMediaLoader.toColor(new Color(rgba));
    }

    @Override
    public int getAlpha(int x, int y) {
        loadTextureData();
        int rgba = textureData.getPixel(region.x() + x, region.y() + y);
        return Math.round(new Color(rgba).a * 100f);
    }
}
