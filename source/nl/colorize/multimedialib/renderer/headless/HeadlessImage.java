//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.math.Region;

public class HeadlessImage implements Image {

    private int width;
    private int height;

    public HeadlessImage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public HeadlessImage() {
        this(100, 100);
    }

    @Override
    public Region getRegion() {
        return new Region(0, 0, width, height);
    }

    @Override
    public Image extractRegion(Region region) {
        return new HeadlessImage(region.width(), region.height());
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        return ColorRGB.BLACK;
    }

    @Override
    public int getAlpha(int x, int y) {
        return 100;
    }

    @Override
    public Image tint(ColorRGB color) {
        return this;
    }
}
