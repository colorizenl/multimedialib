//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.headless;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

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
    public Rect getRegion() {
        return new Rect(0, 0, width, height);
    }

    @Override
    public Image extractRegion(Rect region) {
        return this;
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
