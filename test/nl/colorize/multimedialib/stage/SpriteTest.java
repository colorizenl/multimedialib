//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.scene.Timer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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
        Image first = new MockImage("first", 100, 100);
        Image second = new MockImage("second", 100, 100);
        Image third = new MockImage("third", 100, 100);
        Image fourth = new MockImage(100, 100);

        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new Animation(List.of(first, second, third), 1f, false));
        sprite.addGraphics("b", fourth);

        sprite.animate(Timer.at(1f));
        sprite.animate(Timer.at(10f));

        assertEquals(third, sprite.getCurrentGraphics());

        sprite.changeGraphics("b");
        sprite.changeGraphics("a");

        assertEquals(first, sprite.getCurrentGraphics());
    }

    @Test
    public void testCannotAnimateSpriteWithoutStates() {
        Sprite sprite = new Sprite();
        assertThrows(IllegalStateException.class, () -> sprite.animate(Timer.at(1f)));
    }

    @Test
    void getStateNames() {
        Sprite sprite = new Sprite();
        sprite.addGraphics("a", new MockImage());
        sprite.addGraphics("b", new MockImage());

        assertEquals(Set.of("a", "b"), sprite.getPossibleStates());
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
