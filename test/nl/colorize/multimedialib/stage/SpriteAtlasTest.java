//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpriteAtlasTest {

    @Test
    void getSubImage() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("b", new MockImage(), new Region(0, 0, 50, 50));

        assertEquals(100, atlas.get("a").getWidth());
        assertEquals(50, atlas.get("b").getWidth());
    }

    @Test
    void filterSubImages() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a1", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("a2", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("b", new MockImage(), new Region(0, 0, 50, 50));

        assertEquals(2, atlas.get(name -> name.startsWith("a")).size());
        assertEquals(1, atlas.get(name -> name.startsWith("b")).size());
    }

    @Test
    void doNotAllowMultipleSubImagesWithSameName() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));

        assertThrows(IllegalArgumentException.class, () -> {
            atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));
        });
    }

    @Test
    void merge() {
        SpriteAtlas atlasA = new SpriteAtlas();
        atlasA.add("a1", new MockImage(), new Region(0, 0, 100, 100));
        atlasA.add("a2", new MockImage(), new Region(0, 0, 100, 200));

        SpriteAtlas atlasB = new SpriteAtlas();
        atlasB.add("b", new MockImage(), new Region(0, 0, 100, 300));

        SpriteAtlas merged = atlasA.merge(atlasB);

        assertEquals(100, merged.get("a1").getHeight());
        assertEquals(200, merged.get("a2").getHeight());
        assertEquals(300, merged.get("b").getHeight());
    }

    @Test
    void cannotMergeIfSameName() {
        SpriteAtlas atlasA = new SpriteAtlas();
        atlasA.add("a", new MockImage(), new Region(0, 0, 100, 100));

        SpriteAtlas atlasB = new SpriteAtlas();
        atlasB.add("a", new MockImage(), new Region(0, 0, 100, 200));

        assertThrows(IllegalArgumentException.class, () -> atlasA.merge(atlasB));
    }
}
