//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AWTImageTest {

    @Test
    public void testGetColor() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(1, 0, Color.GREEN.getRGB());
        image.setRGB(2, 0, Color.BLUE.getRGB());

        AWTImage texture = new AWTImage(image);

        assertEquals(ColorRGB.RED, texture.getColor(0, 0));
        assertEquals(ColorRGB.GREEN, texture.getColor(1, 0));
        assertEquals(ColorRGB.BLUE, texture.getColor(2, 0));
    }

    @Test
    public void testGetAlpha() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, new Color(255, 0, 0).getRGB());
        image.setRGB(1, 0, new Color(255, 0, 0, 127).getRGB());
        image.setRGB(2, 0, new Color(255, 0, 0, 0).getRGB());

        AWTImage texture = new AWTImage(image);

        assertEquals(100, texture.getAlpha(0, 0));
        assertEquals(50, texture.getAlpha(1, 0));
        assertEquals(0, texture.getAlpha(2, 0));
    }

    @Test
    public void testExtractRegion() {
        BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(1, 0, Color.GREEN.getRGB());
        image.setRGB(2, 0, Color.BLUE.getRGB());

        AWTImage texture = new AWTImage(image);
        Image region = texture.extractRegion(new Rect(1, 0, 2, 1));

        assertEquals(2, region.getWidth());
        assertEquals(1, region.getHeight());
        assertEquals(ColorRGB.GREEN, region.getColor(0, 0));
        assertEquals(ColorRGB.BLUE, region.getColor(1, 0));
    }
}
