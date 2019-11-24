//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

public class TeaImage implements Image {

    private String id;
    private String url;
    private Rect region;

    protected TeaImage(String id, String url, Rect region) {
        this.id = id;
        this.url = url;
        this.region = region;
    }

    public String getId() {
        return id;
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

    public Rect getRegion() {
        return region;
    }

    @Override
    public Image getRegion(Rect region) {
        return new TeaImage(id, url, region);
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAlpha(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TeaImage) {
            TeaImage other = (TeaImage) o;
            return id.equals(other.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return url;
    }
}
