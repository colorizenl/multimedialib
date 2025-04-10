//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorRGBTest {

    @Test
    void interpolate() {
        ColorRGB start = new ColorRGB(255, 0, 0);
        ColorRGB target = new ColorRGB(0, 0, 0);

        List<ColorRGB> colors = start.interpolate(target, 4);

        assertEquals(4, colors.size());
        assertEquals(255, colors.get(0).r());
        assertEquals(170, colors.get(1).r());
        assertEquals(85, colors.get(2).r());
        assertEquals(0, colors.get(3).r());
    }

    @Test
    void interpolateMultipleColors() {
        ColorRGB start = new ColorRGB(50, 100, 200);
        ColorRGB target = new ColorRGB(100, 50, 255);

        List<ColorRGB> colors = start.interpolate(target, 3);

        assertEquals(3, colors.size());
        assertEquals("#3264C8", colors.get(0).toHex());
        assertEquals("#4B4BE3", colors.get(1).toHex());
        assertEquals("#6432FF", colors.get(2).toHex());
    }

    @Test
    void getRGB() {
        assertEquals(0x000000, ColorRGB.BLACK.getRGB());
        assertEquals(0xFFFFFF, ColorRGB.WHITE.getRGB());
        assertEquals(0xFF0000, ColorRGB.RED.getRGB());
    }

    @Test
    void alter() {
        ColorRGB color = new ColorRGB(255, 0, 0);

        assertEquals(new ColorRGB(255, 100, 50), color.alter(100, 100, 50));
        assertEquals(new ColorRGB(205, 0, 0), color.alter(-50, 0, 0));
        assertEquals(new ColorRGB(0, 0, 0), color.alter(-300, 0, 0));
    }
}
