//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.util.TupleList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void rename() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("b", new MockImage(), new Region(0, 0, 50, 50));
        SpriteAtlas renamed = atlas.rename(name -> "x" + name);

        assertEquals(100, renamed.get("xa").getWidth());
        assertEquals(50, renamed.get("xb").getWidth());
        assertThrows(NoSuchElementException.class, () -> renamed.get("a"));
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
    void mergeFactoryMethod() {
        SpriteAtlas atlasA = new SpriteAtlas();
        atlasA.add("a1", new MockImage(), new Region(0, 0, 100, 100));
        atlasA.add("a2", new MockImage(), new Region(0, 0, 100, 200));

        SpriteAtlas atlasB = new SpriteAtlas();
        atlasB.add("b", new MockImage(), new Region(0, 0, 100, 300));

        SpriteAtlas merged = SpriteAtlas.merge(List.of(atlasA, atlasB));

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

    @Test
    void registerAnimations() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("b", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("c", new MockImage(), new Region(0, 0, 100, 100));
        atlas.addAnimation("x", TupleList.of("a", 0.5, "b", 0.5), false);
        atlas.addAnimation("y", TupleList.of("c", 0.5, "b", 0.5), true);

        assertEquals(Set.of("a", "b", "c"), atlas.getSubImageNames());
        assertEquals(Set.of("x", "y"), atlas.getAnimationNames());
    }

    @Test
    void filterImagesAndAnimations() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("b", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("c", new MockImage(), new Region(0, 0, 100, 100));
        atlas.addAnimation("x", TupleList.of("a", 0.5, "b", 0.5), false);
        atlas.addAnimation("y", TupleList.of("c", 0.5, "b", 0.5), true);

        SpriteAtlas filtered = atlas.filter(
            name -> List.of("a", "b").contains(name),
            name -> name.startsWith("x")
        );

        assertTrue(filtered.contains("a"));
        assertTrue(filtered.contains("b"));
        assertFalse(filtered.contains("c"));
        assertTrue(filtered.containsAnimation("x"));
        assertFalse(filtered.containsAnimation("y"));
    }

    @Test
    void filterAnimations() {
        SpriteAtlas atlas = new SpriteAtlas();
        atlas.add("a", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("b", new MockImage(), new Region(0, 0, 100, 100));
        atlas.add("c", new MockImage(), new Region(0, 0, 100, 100));
        atlas.addAnimation("x", TupleList.of("a", 0.5, "b", 0.5), false);
        atlas.addAnimation("y", TupleList.of("c", 0.5, "b", 0.5), true);

        SpriteAtlas filtered = atlas.filterAnimations(name -> name.startsWith("x"));

        assertFalse(filtered.contains("a"));
        assertFalse(filtered.contains("b"));
        assertFalse(filtered.contains("c"));
        assertTrue(filtered.containsAnimation("x"));
        assertFalse(filtered.containsAnimation("y"));
    }
}
