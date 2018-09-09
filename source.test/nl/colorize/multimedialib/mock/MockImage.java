//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;

/**
 * Mock implementation of the {@code Image} and {@code ImageRegion} interfaces.
 */
public class MockImage implements Image {
    
    private int width;
    private int height;

    public MockImage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public MockImage() {
        this(128, 128);
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getHeight() {
        return height;
    }

    public Image getRegion(Rect region) {
        return new MockImage(region.getWidth(), region.getHeight());
    }
}
