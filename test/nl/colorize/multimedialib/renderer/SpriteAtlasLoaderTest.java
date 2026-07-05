//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.multimedialib.stage.Animation;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.colorize.multimedialib.math.Point2D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteAtlasLoaderTest {

    @Test
    void loadAtlas(@TempDir File tempDir) throws IOException {
        String contents = """
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
            b
              xy: 100, 202
              size: 50, 50
            """;

        Files.writeString(new File(tempDir, "test.atlas").toPath(), contents, UTF_8);
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "test.png"));

        SpriteAtlasLoader loader = new SpriteAtlasLoader(getTempDirMediaLoader(tempDir));
        SpriteAtlas atlas = loader.load(new ResourceFile("test.atlas"));

        assertEquals(100, atlas.get("a").getWidth());
        assertEquals(50, atlas.get("b").getWidth());
    }

    @Test
    void loadMultiImageAtlas(@TempDir File tempDir) throws IOException {
        String contents = """
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
              
            other.png
            b
              xy: 100, 202
              size: 50, 50
            """;

        Files.writeString(new File(tempDir, "test.atlas").toPath(), contents, UTF_8);
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "test.png"));
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "other.png"));

        SpriteAtlasLoader loader = new SpriteAtlasLoader(getTempDirMediaLoader(tempDir));
        SpriteAtlas atlas = loader.load(new ResourceFile("test.atlas"));

        assertEquals(100, atlas.get("a").getWidth());
        assertEquals(50, atlas.get("b").getWidth());
    }

    @Test
    void loadAnimFile(@TempDir File tempDir) throws IOException {
        String atlasFile = """
            test.png
            size: 256, 256
            format: RGBA8888
            filter: Nearest, Nearest
            repeat: none
            a
              xy: 0, 0
              size: 128, 128
            b
              xy: 128, 0
              size: 64, 64
            """;

        String animFile = """
            walk
              a: 1
              b: 0.5
              a: 0.5
            
            other:
              b: 1
              loop: true
            """;

        Files.writeString(new File(tempDir, "test.atlas").toPath(), atlasFile, UTF_8);
        Files.writeString(new File(tempDir, "test.anim").toPath(), animFile, UTF_8);
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "test.png"));

        SpriteAtlasLoader loader = new SpriteAtlasLoader(getTempDirMediaLoader(tempDir));
        SpriteAtlas atlas = loader.load(new ResourceFile("test.atlas"));

        Animation walk = atlas.getAnimation("walk");
        Animation other = atlas.getAnimation("other");

        assertEquals(3, walk.getFrameCount());
        assertEquals(1.0, walk.getFrameTime(0), EPSILON);
        assertEquals(0.5, walk.getFrameTime(1), EPSILON);
        assertEquals(0.5, walk.getFrameTime(2), EPSILON);
        assertFalse(walk.isLoop());

        assertEquals(1, other.getFrameCount());
        assertEquals(1.0, other.getFrameTime(0), EPSILON);
        assertTrue(other.isLoop());
    }

    private MediaLoader getTempDirMediaLoader(File tempDir) {
        return new StandardMediaLoader() {
            @Override
            protected ResourceFile locateFile(ResourceFile location) {
                File tempFile = new File(tempDir, location.path());
                return new ResourceFile(tempFile);
            }
        };
    }
}
