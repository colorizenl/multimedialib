//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Sprite;

@Getter
@Setter
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
    public Region getRegion() {
        return new Region(0, 0, width, height);
    }

    @Override
    public Image extractRegion(Region region) {
        return new MockImage(name + "@" + region, region.width(), region.height());
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        return ColorRGB.RED;
    }

    @Override
    public int getAlpha(int x, int y) {
        return 100;
    }

    public Sprite toSprite() {
        return new Sprite(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
