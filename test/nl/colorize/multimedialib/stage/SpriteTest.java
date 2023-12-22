//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpriteTest {

    @Test
    public void testChangeState() {
        Image imageA = new MockImage(100, 100);
        Image imageB = new MockImage(100, 100);

        Sprite sprite = new Sprite();
        sprite.addGraphics("a", imageA);
        sprite.addGraphics("b", imageB);

        assertEquals("a", sprite.getActiveState());
        assertEquals(imageA, sprite.getCurrentGraphics());

        sprite.changeGraphics("b");

        assertEquals("b", sprite.getActiveState());
        assertEquals(imageB, sprite.getCurrentGraphics());
    }

    @Test
    public void testCannotAddSameStateTwice() {
        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage(100, 100));

        assertThrows(IllegalArgumentException.class, () -> {
            sprite.addGraphics("a", new MockImage(100, 100));
        });
    }

    @Test
    public void testChangingStateResetsAnimation() {
        Image first = new MockImage(100, 100);
        Image second = new MockImage(100, 100);
        Image third = new MockImage(100, 100);
        Animation animation = new Animation(ImmutableList.of(first, second, third), 1f, false);

        Sprite sprite = new Sprite();
        sprite.addGraphics("a", animation);
        sprite.addGraphics("b", new MockImage(100, 100));

        sprite.update(1f);
        sprite.update(1f);

        assertEquals(third, sprite.getCurrentGraphics());

        sprite.changeGraphics("b");
        sprite.changeGraphics("a");

        assertEquals(first, sprite.getCurrentGraphics());
    }

    @Test
    public void testCannotAnimateSpriteWithoutStates() {
        Sprite sprite = new Sprite();
        assertThrows(IllegalStateException.class, () -> sprite.update(1f));
    }

    @Test
    void getStateNames() {
        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage());
        sprite.addGraphics("b", new MockImage());

        assertEquals(ImmutableSet.of("a", "b"), sprite.getPossibleStates());
    }

    @Test
    void copyShouldCreateDeepCopy() {
        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage());
        sprite.addGraphics("b", new MockImage());
        sprite.changeGraphics("a");

        Sprite copy = sprite.copy();
        copy.changeGraphics("b");

        assertEquals("a", sprite.getActiveState());
        assertEquals("b", copy.getActiveState());
    }
}
