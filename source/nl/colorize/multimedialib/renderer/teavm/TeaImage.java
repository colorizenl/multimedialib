//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.FilePointer;

import java.util.UUID;

public class TeaImage implements Image {

    private String id;
    private FilePointer origin;
    private Rect region;

    private int cachedWidth;
    private int cachedHeight;

    protected TeaImage(String id, FilePointer origin, Rect region) {
        Preconditions.checkArgument(region == null || (region.getWidth() > 0 && region.getHeight() > 0),
            "Invalid region for image " + origin + ": " + region);

        this.id = id;
        this.origin = origin;
        this.region = region;
    }

    public String getId() {
        return id;
    }

    public FilePointer getOrigin() {
        return origin;
    }

    private void cacheImageSize() {
        if (region == null || region.getWidth() == 0 || region.getHeight() == 0) {
            region = new Rect(0f, 0f, Browser.getImageWidth(id), Browser.getImageHeight(id));
        }

        if (cachedWidth == 0 || cachedHeight == 0) {
            cachedWidth = Math.round(region.getWidth());
            cachedHeight = Math.round(region.getHeight());
        }
    }

    @Override
    public Rect getRegion() {
        cacheImageSize();
        return region;
    }

    @Override
    public int getWidth() {
        cacheImageSize();
        return cachedWidth;
    }

    @Override
    public int getHeight() {
        cacheImageSize();
        return cachedHeight;
    }

    @Override
    public TeaImage extractRegion(Rect region) {
        return new TeaImage(id, origin, region);
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        float[] rgba = Browser.getImageData(id, x, y);
        int r = Math.round(rgba[0]);
        int g = Math.round(rgba[1]);
        int b = Math.round(rgba[2]);

        if (r < 0) {
            return null;
        }

        return new ColorRGB(r, g, b);
    }

    @Override
    public int getAlpha(int x, int y) {
        float[] rgba = Browser.getImageData(id, x, y);
        return Math.round(rgba[3] / 2.55f);
    }

    @Override
    public Image tint(ColorRGB color) {
        String newId = id + "-tinted-" + UUID.randomUUID();
        Browser.tintImage(id, newId, color.toHex());
        return new TeaImage(newId, origin, region);
    }

    @Override
    public String toString() {
        return origin.getPath();
    }
}
