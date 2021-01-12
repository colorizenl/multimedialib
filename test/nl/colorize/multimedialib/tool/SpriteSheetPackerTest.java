//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.colorize.util.FileUtils;
import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        spriteSheetPacker.metadataFormat = "yaml";
        spriteSheetPacker.size = 512;
        spriteSheetPacker.run();

        String expected = "";
        expected += "- name: a\n";
        expected += "  x: 103\n";
        expected += "  y: 1\n";
        expected += "  width: 100\n";
        expected += "  height: 200\n";
        expected += "- name: b\n";
        expected += "  x: 1\n";
        expected += "  y: 1\n";
        expected += "  width: 100\n";
        expected += "  height: 300\n";

        assertTrue(spriteSheetPacker.outputImageFile.exists());
        assertTrue(spriteSheetPacker.outputDataFile.exists());
        assertEquals(expected, Files.toString(spriteSheetPacker.outputDataFile, Charsets.UTF_8));
    }

    @Test
    public void testExplicitlyExcludeLargeImages() throws IOException {
        File tempDir = FileUtils.createTempDir();
        BufferedImage imageA = Utils2D.createTestImage(100, 100);
        Utils2D.savePNG(imageA, new File(tempDir, "a.png"));
        BufferedImage imageB = Utils2D.createTestImage(500, 500);
        Utils2D.savePNG(imageB, new File(tempDir, "b.png"));

        File outputDir = FileUtils.createTempDir();

        SpriteSheetPacker spriteSheetPacker = new SpriteSheetPacker();
        spriteSheetPacker.inputDir = tempDir;
        spriteSheetPacker.outputImageFile = new File(outputDir, "test.png");
        spriteSheetPacker.outputDataFile = new File(outputDir, "test.yaml");
        spriteSheetPacker.metadataFormat = "yaml";
        spriteSheetPacker.size = 512;
        spriteSheetPacker.excludeSize = 300;
        spriteSheetPacker.run();

        String expected = "";
        expected += "- name: a\n";
        expected += "  x: 1\n";
        expected += "  y: 1\n";
        expected += "  width: 100\n";
        expected += "  height: 100\n";

        assertEquals(expected, Files.toString(spriteSheetPacker.outputDataFile, Charsets.UTF_8));
    }

    @Test
    public void testImplicitlyExcludeLargeImages() throws IOException {
        File tempDir = FileUtils.createTempDir();
        BufferedImage imageA = Utils2D.createTestImage(100, 100);
        Utils2D.savePNG(imageA, new File(tempDir, "a.png"));
        BufferedImage imageB = Utils2D.createTestImage(500, 500);
        Utils2D.savePNG(imageB, new File(tempDir, "b.png"));

        File outputDir = FileUtils.createTempDir();

        SpriteSheetPacker spriteSheetPacker = new SpriteSheetPacker();
        spriteSheetPacker.inputDir = tempDir;
        spriteSheetPacker.outputImageFile = new File(outputDir, "test.png");
        spriteSheetPacker.outputDataFile = new File(outputDir, "test.yaml");
        spriteSheetPacker.metadataFormat = "yaml";
        spriteSheetPacker.size = 256;
        spriteSheetPacker.run();

        String expected = "";
        expected += "- name: a\n";
        expected += "  x: 1\n";
        expected += "  y: 1\n";
        expected += "  width: 100\n";
        expected += "  height: 100\n";

        assertEquals(expected, Files.toString(spriteSheetPacker.outputDataFile, Charsets.UTF_8));
    }

    @Test
    public void testUseRelativePathAsImageName() throws IOException {
        File tempDir = FileUtils.createTempDir();
        File subDir = new File(tempDir, "sub");
        subDir.mkdir();
        Utils2D.savePNG(Utils2D.createTestImage(100, 100), new File(subDir, "a.png"));

        File outputDir = FileUtils.createTempDir();

        SpriteSheetPacker spriteSheetPacker = new SpriteSheetPacker();
        spriteSheetPacker.inputDir = tempDir;
        spriteSheetPacker.outputImageFile = new File(outputDir, "test.png");
        spriteSheetPacker.outputDataFile = new File(outputDir, "test.yaml");
        spriteSheetPacker.metadataFormat = "yaml";
        spriteSheetPacker.size = 256;
        spriteSheetPacker.run();

        String expected = "";
        expected += "- name: sub/a\n";
        expected += "  x: 1\n";
        expected += "  y: 1\n";
        expected += "  width: 100\n";
        expected += "  height: 100\n";

        assertEquals(expected, Files.toString(spriteSheetPacker.outputDataFile, Charsets.UTF_8));
    }

    @Test
    public void testExportMetadataToCSV() throws IOException {
        File tempDir = FileUtils.createTempDir();
        BufferedImage imageA = Utils2D.createTestImage(100, 100);
        Utils2D.savePNG(imageA, new File(tempDir, "a.png"));
        BufferedImage imageB = Utils2D.createTestImage(200, 110);
        Utils2D.savePNG(imageB, new File(tempDir, "b.png"));

        File outputDir = FileUtils.createTempDir();

        SpriteSheetPacker spriteSheetPacker = new SpriteSheetPacker();
        spriteSheetPacker.inputDir = tempDir;
        spriteSheetPacker.outputImageFile = new File(outputDir, "test.png");
        spriteSheetPacker.outputDataFile = new File(outputDir, "test.csv");
        spriteSheetPacker.metadataFormat = "csv";
        spriteSheetPacker.size = 256;
        spriteSheetPacker.run();

        String expected = "";
        expected += "name;x;y;width;height\n";
        expected += "a;1;113;100;100\n";
        expected += "b;1;1;200;110";

        assertEquals(expected, Files.toString(spriteSheetPacker.outputDataFile, Charsets.UTF_8));
    }
}
