//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.collect.ImmutableSet;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpriteSheetTest {

    @Test
    public void testMarkRegion() {
        SpriteSheet spriteSheet = new SpriteSheet(new MockImage(100, 100));
        spriteSheet.markRegion("a", new Rect(0, 0, 10, 10));
        spriteSheet.markRegion("b", new Rect(10, 10, 10, 10));

        assertEquals(10, spriteSheet.get("a").getWidth());
        assertEquals(10, spriteSheet.get("a").getHeight());
        assertEquals(new Rect(0, 0, 10, 10), spriteSheet.getRegion("a"));
        assertEquals(ImmutableSet.of("a", "b"), spriteSheet.getRegionNames());
    }

    @Test
    public void testGetSequence() {
        SpriteSheet spriteSheet = new SpriteSheet(new MockImage(100, 100));
        Image imageA = spriteSheet.markRegion("a", new Rect(0, 0, 10, 10));
        Image imageB = spriteSheet.markRegion("b", new Rect(10, 10, 10, 10));
        spriteSheet.markRegion("c", new Rect(20, 20, 10, 10));

        List<Image> sequence = spriteSheet.get("a", "b");

        assertEquals(2, sequence.size());
        assertEquals(imageA, sequence.get(0));
        assertEquals(imageB, sequence.get(1));
    }

    @Test
    public void testOutOfBoundsNotAllowed() {
        SpriteSheet spriteSheet = new SpriteSheet(new MockImage(100, 100));

        assertThrows(IllegalArgumentException.class, () -> {
            spriteSheet.markRegion("a", new Rect(95, 0, 10, 10));
        });
    }

    @Test
    public void testDuplicateRegionNotAllowed() {
        SpriteSheet spriteSheet = new SpriteSheet(new MockImage(100, 100));
        spriteSheet.markRegion("a", new Rect(0, 0, 10, 10));

        assertThrows(IllegalArgumentException.class, () -> {
            spriteSheet.markRegion("a", new Rect(10, 10, 10, 10));
        });
    }
}
