//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.jfx;

import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import lombok.Getter;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;

/**
 * Image loaded with JavaFX, which does not use the Java2D and ImageIO APIs
 * that are part of the standard library.
 */
@Getter
public class JFXImage implements Image {

    private javafx.scene.image.Image image;
    private Region region;
    private PixelReader pixels;

    protected JFXImage(javafx.scene.image.Image image, Region region) {
        this.image = image;
        this.region = region;
    }

    @Override
    public JFXImage extractRegion(Region subRegion) {
        return new JFXImage(image, subRegion);
    }

    private void readPixels() {
        if (pixels == null) {
            pixels = image.getPixelReader();
        }
    }

    @Override
    public ColorRGB getColor(int x, int y) {
        readPixels();

        Color pixel = pixels.getColor(x, y);
        int red = (int) Math.round(pixel.getRed() * 255);
        int green = (int) Math.round(pixel.getGreen() * 255);
        int blue = (int) Math.round(pixel.getBlue() * 255);

        return new ColorRGB(red, green, blue);
    }

    @Override
    public int getAlpha(int x, int y) {
        readPixels();
        Color pixel = pixels.getColor(x, y);
        return (int) Math.round(pixel.getOpacity() * 100);
    }
}
