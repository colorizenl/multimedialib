//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.util.LogHelper;

import java.util.UUID;
import java.util.logging.Logger;

public class TeaImage implements Image {

    private String id;
    private FilePointer origin;
    private Region region;

    private static final Logger LOGGER = LogHelper.getLogger(TeaImage.class);

    protected TeaImage(String id, FilePointer origin, Region region) {
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
    public Region getRegion() {
        if (region == null) {
            int width = (int) Browser.getImageWidth(id);
            int height = (int) Browser.getImageHeight(id);

            // Unlike other renderers, images are loaded asynchronously.
            // Application logic should not run before all required images
            // have been loaded, but if this somehow happens anyway we
            // return a non-zero region to prevent the application code
            // from crashing.
            if (width == 0 || height == 0) {
                LOGGER.warning("Image data not yet available for " + this);
                return new Region(0, 0, 1, 1);
            }

            region = new Region(0, 0, width, height);
        }

        return region;
    }

    @Override
    public TeaImage extractRegion(Region region) {
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
        if (origin == null) {
            return "TeaImage";
        }
        return origin.toString();
    }
}
