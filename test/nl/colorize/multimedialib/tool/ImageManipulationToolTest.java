//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageManipulationToolTest {

    @Test
    void horizontalOffset(@TempDir File tempDir, @TempDir File outputDir) throws IOException {
        BufferedImage testImage = Utils2D.createTestImage(20, 20);
        Utils2D.savePNG(testImage, new File(tempDir, "test.png"));

        ImageManipulationTool tool = new ImageManipulationTool();
        tool.inputDir = tempDir;
        tool.outputDir = outputDir;
        tool.outputPrefix = "test-out-";
        tool.horizontalOffset = 10;
        tool.run();

        assertTrue(new File(outputDir, "test-out-1.png").exists());
        assertTrue(new File(outputDir, "test-out-2.png").exists());
        assertTrue(new File(outputDir, "test-out-3.png").exists());
        assertFalse(new File(outputDir, "test-out-4.png").exists());
    }

    @Test
    void verticalOffset(@TempDir File tempDir, @TempDir File outputDir) throws IOException {
        BufferedImage testImage = Utils2D.createTestImage(20, 20);
        Utils2D.savePNG(testImage, new File(tempDir, "test.png"));

        ImageManipulationTool tool = new ImageManipulationTool();
        tool.inputDir = tempDir;
        tool.outputDir = outputDir;
        tool.outputPrefix = "test-out-";
        tool.verticalOffset = 10;
        tool.run();

        assertTrue(new File(outputDir, "test-out-1.png").exists());
        assertTrue(new File(outputDir, "test-out-2.png").exists());
        assertTrue(new File(outputDir, "test-out-3.png").exists());
        assertFalse(new File(outputDir, "test-out-4.png").exists());
    }

    @Test
    void mirrorImage(@TempDir File tempDir, @TempDir File outputDir) throws IOException {
        BufferedImage testImage = Utils2D.createTestImage(20, 20);
        Utils2D.savePNG(testImage, new File(tempDir, "test.png"));

        ImageManipulationTool tool = new ImageManipulationTool();
        tool.inputDir = tempDir;
        tool.outputDir = outputDir;
        tool.outputPrefix = "test-out-";
        tool.mirror = true;
        tool.run();

        assertTrue(new File(outputDir, "test-out-1.png").exists());
        assertFalse(new File(outputDir, "test-out-2.png").exists());
    }
}
