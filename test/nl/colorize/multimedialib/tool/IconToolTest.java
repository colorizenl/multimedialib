//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.Platform;
import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IconToolTest {

    @Test
    void generateICNS(@TempDir File tempDir) throws IOException {
        BufferedImage testImage = Utils2D.createTestImage(128, 128);
        File pngFile = new File(tempDir, "test.png");
        Utils2D.savePNG(testImage, pngFile);

        IconTool tool = new IconTool();
        tool.inputFile = pngFile;
        tool.outputDir = tempDir;
        tool.run();

        assertTrue(new File(tempDir, "icon.iconset").exists());
        assertTrue(new File(tempDir, "icon.iconset/icon_512x512.png").exists());
        assertTrue(new File(tempDir, "icon.iconset/icon_512x512@2x.png").exists());

        if (Platform.isMac()) {
            assertTrue(new File(tempDir, "icon.icns").exists());
        }
    }

    @Test
    void generateBrowserIcons(@TempDir File tempDir) throws IOException {
        BufferedImage testImage = Utils2D.createTestImage(128, 128);
        File pngFile = new File(tempDir, "test.png");
        Utils2D.savePNG(testImage, pngFile);

        IconTool tool = new IconTool();
        tool.inputFile = pngFile;
        tool.outputDir = tempDir;
        tool.run();

        assertTrue(new File(tempDir, "favicon.png").exists());
        assertTrue(new File(tempDir, "apple-favicon.png").exists());
        assertTrue(new File(tempDir, "icon-512.png").exists());
        assertTrue(new File(tempDir, "icon-192.png").exists());
    }
}
