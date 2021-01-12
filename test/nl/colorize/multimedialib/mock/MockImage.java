//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

/**
 * Mock implementation of the {@code Image} and {@code ImageRegion} interfaces.
 */
public class MockImage implements Image {

    private String name;
    private int width;
    private int height;

    public MockImage(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public MockImage(int width, int height) {
        this("MockImage", width, height);
    }

    public MockImage(String name) {
        this(name, 128, 128);
    }

    public MockImage() {
        this(128, 128);
    }

    @Override
    public Rect getRegion() {
        return new Rect(0, 0, width, height);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Image extractRegion(Rect region) {
        return new MockImage(Math.round(region.getWidth()), Math.round(region.getHeight()));
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        return ColorRGB.RED;
    }

    @Override
    public int getAlpha(int x, int y) {
        return 100;
    }

    @Override
    public Image tint(ColorRGB color) {
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
