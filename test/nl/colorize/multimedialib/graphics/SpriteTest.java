//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.collect.ImmutableList;
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
        sprite.addState("a", imageA);
        sprite.addState("b", imageB);

        assertEquals("a", sprite.getActiveState());
        assertEquals(imageA, sprite.getCurrentGraphics());

        sprite.changeState("b");

        assertEquals("b", sprite.getActiveState());
        assertEquals(imageB, sprite.getCurrentGraphics());
    }

    @Test
    public void testCannotAddSameStateTwice() {
        Sprite sprite = new Sprite();
        sprite.addState("a", new MockImage(100, 100));

        assertThrows(IllegalArgumentException.class, () -> {
            sprite.addState("a", new MockImage(100, 100));
        });
    }

    @Test
    public void testChangingStateResetsAnimation() {
        Image first = new MockImage(100, 100);
        Image second = new MockImage(100, 100);
        Image third = new MockImage(100, 100);
        Animation animation = new Animation(ImmutableList.of(first, second, third), 1f, false);

        Sprite sprite = new Sprite();
        sprite.addState("a", animation);
        sprite.addState("b", new MockImage(100, 100));

        sprite.update(1f);
        sprite.update(1f);

        assertEquals(third, sprite.getCurrentGraphics());

        sprite.changeState("b");
        sprite.changeState("a");

        assertEquals(first, sprite.getCurrentGraphics());
    }

    @Test
    public void testCannotAnimateSpriteWithoutStates() {
        Sprite sprite = new Sprite();
        assertThrows(IllegalStateException.class, () -> sprite.update(1f));
    }
}
