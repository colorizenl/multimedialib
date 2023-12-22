//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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

class MacIconToolTest {

    @Test
    void generateICNS(@TempDir File tempDir) throws IOException {
        BufferedImage testImage = Utils2D.createTestImage(128, 128);
        File pngFile = new File(tempDir, "test.png");
        Utils2D.savePNG(testImage, pngFile);

        if (Platform.isMac()) {
            MacIconTool tool = new MacIconTool();
            tool.inputFile = pngFile;
            tool.outputDir = tempDir;
            tool.run();

            assertTrue(new File(tempDir, "icon.icns").exists());
        }
    }
}
