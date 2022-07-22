//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageManipulationToolTest {

    @Test
    void changeImageAlpha(@TempDir File tempDir, @TempDir File outputDir) throws IOException {
        BufferedImage image = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, new Color(255, 0, 0, 255).getRGB());
        image.setRGB(1, 0, new Color(255, 0, 0, 127).getRGB());
        image.setRGB(2, 0, new Color(255, 0, 0, 0).getRGB());

        File tempFile = new File(tempDir, "test.png");
        Utils2D.savePNG(image, tempFile);

        ImageManipulationTool tool = new ImageManipulationTool();
        tool.inputDir = tempDir;
        tool.outputDir = outputDir;
        tool.alphaChannel = "#FF0000";
        tool.run();

        BufferedImage result = Utils2D.loadImage(new File(outputDir, "test.png"));

        assertEquals(new Color(0, 0, 0, 0).getRGB(), result.getRGB(0, 0));
        assertEquals(new Color(255, 0, 0, 127).getRGB(), result.getRGB(1, 0));
        assertEquals(new Color(255, 0, 0, 0).getRGB(), result.getRGB(2, 0));
    }
}
