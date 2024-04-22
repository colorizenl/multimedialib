//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Charsets;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        Files.writeString(new File(tempDir, "test.atlas").toPath(), contents, Charsets.UTF_8);
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "test.png"));

        MediaLoader mediaLoader = new StandardMediaLoader() {
            @Override
            protected ResourceFile locateFile(FilePointer location) {
                return new ResourceFile(new File(tempDir, location.path()));
            }
        };
        SpriteAtlasLoader loader = new SpriteAtlasLoader(mediaLoader);
        SpriteAtlas atlas = loader.load(new FilePointer("test.atlas"));

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

        Files.writeString(new File(tempDir, "test.atlas").toPath(), contents, Charsets.UTF_8);
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "test.png"));
        Utils2D.savePNG(Utils2D.createTestImage(256, 256), new File(tempDir, "other.png"));

        MediaLoader mediaLoader = new StandardMediaLoader() {
            @Override
            protected ResourceFile locateFile(FilePointer location) {
                return new ResourceFile(new File(tempDir, location.path()));
            }
        };
        SpriteAtlasLoader loader = new SpriteAtlasLoader(mediaLoader);
        SpriteAtlas atlas = loader.load(new FilePointer("test.atlas"));

        assertEquals(100, atlas.get("a").getWidth());
        assertEquals(50, atlas.get("b").getWidth());
    }
}
