//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColorRGBTest {

    @Test
    void interpolate() {
        ColorRGB start = new ColorRGB(255, 0, 0);
        ColorRGB target = new ColorRGB(0, 0, 0);

        List<ColorRGB> colors = start.interpolate(target, 4);

        assertEquals(4, colors.size());
        assertEquals(255, colors.get(0).getR());
        assertEquals(170, colors.get(1).getR());
        assertEquals(85, colors.get(2).getR());
        assertEquals(0, colors.get(3).getR());
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
}
