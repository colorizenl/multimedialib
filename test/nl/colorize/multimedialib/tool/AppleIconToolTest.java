//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.Platform;
import nl.colorize.util.swing.Utils2D;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class AppleIconToolTest {

    @Test
    public void testCreateICNS() throws IOException {
        BufferedImage image = Utils2D.createTestImage(256, 256);
        File tempFile = File.createTempFile("image", ".png");
        Utils2D.savePNG(image, tempFile);

        AppleIconTool tool = new AppleIconTool();
        tool.inputImageFile = tempFile;
        tool.outputFile = File.createTempFile("icon", ".icns");
        tool.run();

        assertTrue(new File(tool.outputFile.getParentFile(), "icon.iconset").exists());
        assertTrue(!Platform.isMac() || tool.outputFile.exists());
    }
}
