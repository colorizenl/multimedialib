//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.colorize.util.FileUtils;
import nl.colorize.util.swing.Utils2D;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpriteSheetPackerTest {

    @Test
    public void testCreateSpriteSheetFromDirectoryWithImages() throws IOException {
        File tempDir = FileUtils.createTempDir();
        BufferedImage imageA = Utils2D.createTestImage(100, 200);
        Utils2D.savePNG(imageA, new File(tempDir, "a.png"));
        BufferedImage imageB = Utils2D.createTestImage(100, 300);
        Utils2D.savePNG(imageB, new File(tempDir, "b.png"));

        File outputDir = FileUtils.createTempDir();

        SpriteSheetPacker spriteSheetPacker = new SpriteSheetPacker();
        spriteSheetPacker.inputDir = tempDir;
        spriteSheetPacker.outputImageFile = new File(outputDir, "test.png");
        spriteSheetPacker.outputDataFile = new File(outputDir, "test.yaml");
        spriteSheetPacker.size = 512;
        spriteSheetPacker.run();

        String expected = "";
        expected += "- name: a.png\n";
        expected += "  x: 100\n";
        expected += "  y: 0\n";
        expected += "  width: 100\n";
        expected += "  height: 200\n";
        expected += "- name: b.png\n";
        expected += "  x: 0\n";
        expected += "  y: 0\n";
        expected += "  width: 100\n";
        expected += "  height: 300\n";

        assertTrue(spriteSheetPacker.outputImageFile.exists());
        assertTrue(spriteSheetPacker.outputDataFile.exists());
        assertEquals(expected, Files.toString(spriteSheetPacker.outputDataFile, Charsets.UTF_8));
    }
}