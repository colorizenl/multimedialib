//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.FilePointer;

import java.util.UUID;

public class TeaImage implements Image {

    private String id;
    private FilePointer origin;
    private Rect region;

    protected TeaImage(String id, FilePointer origin, Rect region) {
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

    @Override
    public Rect getRegion() {
        return region;
    }

    @Override
    public int getWidth() {
        if (region != null) {
            return Math.round(region.getWidth());
        } else {
            return Math.round(Browser.getImageWidth(id));
        }
    }

    @Override
    public int getHeight() {
        if (region != null) {
            return Math.round(region.getHeight());
        } else {
            return Math.round(Browser.getImageHeight(id));
        }
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
