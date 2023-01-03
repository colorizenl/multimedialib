//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpriteAtlasPackerTest {

    @Test
    public void generateSpriteAtlas(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        File imageDir = new File(inputDir, "test");
        imageDir.mkdir();

        BufferedImage imageA = Utils2D.createTestImage(100, 200);
        Utils2D.savePNG(imageA, new File(imageDir, "a.png"));
        BufferedImage imageB = Utils2D.createTestImage(100, 300);
        Utils2D.savePNG(imageB, new File(imageDir, "b.png"));

        SpriteAtlasPacker spriteSheetPacker = new SpriteAtlasPacker();
        spriteSheetPacker.inputDir = imageDir;
        spriteSheetPacker.outputDir = outputDir;
        spriteSheetPacker.run();

        File atlasFile = new File(outputDir, "test.atlas");
        File atlasImageFile = new File(outputDir, "test.png");

        String expected = """
                        
            test.png
            size: 128, 512
            format: RGBA8888
            filter: Nearest, Nearest
            repeat: none
            a
              rotate: false
              xy: 2, 2
              size: 100, 200
              orig: 100, 200
              offset: 0, 0
              index: -1
            b
              rotate: false
              xy: 2, 204
              size: 100, 300
              orig: 100, 300
              offset: 0, 0
              index: -1
            """;

        assertEquals(expected, Files.readString(atlasFile.toPath(), Charsets.UTF_8));
        assertTrue(atlasImageFile.exists());
        assertEquals(512, Utils2D.loadImage(atlasImageFile).getHeight());
    }

    @Test
    public void includeSubDirPath(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        File imageDir = new File(inputDir, "test");
        imageDir.mkdir();

        File subDir = new File(imageDir, "sub");
        subDir.mkdir();

        BufferedImage imageA = Utils2D.createTestImage(100, 200);
        Utils2D.savePNG(imageA, new File(subDir, "a.png"));

        SpriteAtlasPacker spriteSheetPacker = new SpriteAtlasPacker();
        spriteSheetPacker.inputDir = imageDir;
        spriteSheetPacker.outputDir = outputDir;
        spriteSheetPacker.run();

        String expected = """
                        
            test.png
            size: 128, 256
            format: RGBA8888
            filter: Nearest, Nearest
            repeat: none
            sub/a
              rotate: false
              xy: 2, 2
              size: 100, 200
              orig: 100, 200
              offset: 0, 0
              index: -1
            """;

        assertEquals(expected,
            Files.readString(new File(outputDir, "test.atlas").toPath(), Charsets.UTF_8));
    }

    @Test
    public void flattenPathOption(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        File imageDir = new File(inputDir, "test");
        imageDir.mkdir();

        File subDir = new File(imageDir, "sub");
        subDir.mkdir();

        BufferedImage imageA = Utils2D.createTestImage(100, 200);
        Utils2D.savePNG(imageA, new File(subDir, "a.png"));

        SpriteAtlasPacker spriteSheetPacker = new SpriteAtlasPacker();
        spriteSheetPacker.inputDir = imageDir;
        spriteSheetPacker.outputDir = outputDir;
        spriteSheetPacker.flatten = true;
        spriteSheetPacker.run();

        String expected = """
                        
            test.png
            size: 128, 256
            format: RGBA8888
            filter: Nearest, Nearest
            repeat: none
            a
              rotate: false
              xy: 2, 2
              size: 100, 200
              orig: 100, 200
              offset: 0, 0
              index: -1
            """;

        assertEquals(expected,
            Files.readString(new File(outputDir, "test.atlas").toPath(), Charsets.UTF_8));
    }

    @Test
    public void nestedAtlas(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        File subDir1 = new File(inputDir, "sub1");
        subDir1.mkdir();

        File subDir2 = new File(inputDir, "sub2");
        subDir2.mkdir();

        Utils2D.savePNG(Utils2D.createTestImage(100, 100), new File(subDir1, "a.png"));
        Utils2D.savePNG(Utils2D.createTestImage(100, 200), new File(subDir2, "b.png"));

        SpriteAtlasPacker spriteSheetPacker = new SpriteAtlasPacker();
        spriteSheetPacker.inputDir = inputDir;
        spriteSheetPacker.outputDir = outputDir;
        spriteSheetPacker.nested = true;
        spriteSheetPacker.run();

        assertFalse(new File(outputDir, "test.atlas").exists());
        assertTrue(new File(outputDir, "sub1.atlas").exists());
        assertTrue(new File(outputDir, "sub2.atlas").exists());
    }
}
