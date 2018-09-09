//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.mock.MockImage;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpriteTest {

    @Test
    public void testChangeState() {
        Image imageA = new MockImage(100, 100);
        Image imageB = new MockImage(100, 100);

        Sprite sprite = new Sprite();
        sprite.addState("a", imageA);
        sprite.addState("b", imageB);

        assertEquals("a", sprite.getCurrentState());
        assertEquals(imageA, sprite.getCurrentGraphics());

        sprite.changeState("b");

        assertEquals("b", sprite.getCurrentState());
        assertEquals(imageB, sprite.getCurrentGraphics());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCannotAddSameStateTwice() {
        Sprite sprite = new Sprite();
        sprite.addState("a", new MockImage(100, 100));
        sprite.addState("a", new MockImage(100, 100));
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

        sprite.onFrame(1f);
        sprite.onFrame(1f);

        assertEquals(third, sprite.getCurrentGraphics());

        sprite.changeState("b");
        sprite.changeState("a");

        assertEquals(first, sprite.getCurrentGraphics());
    }

    @Test(expected=IllegalStateException.class)
    public void testCannotAnimateSpriteWithoutStates() {
        Sprite sprite = new Sprite();
        sprite.onFrame(1f);
    }
}