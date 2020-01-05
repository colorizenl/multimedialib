//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.mock.MockImage;
import org.junit.Test;

import static org.junit.Assert.*;

public class AnimationTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testCreateRegularAnimation() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");

        Animation anim = new Animation(false);
        anim.addFrame(frameA, 1f);
        anim.addFrame(frameB, 1f);

        assertEquals(2, anim.getFrameCount());
        assertEquals(frameA, anim.getFrameAtIndex(0));
        assertEquals(frameB, anim.getFrameAtIndex(1));
        assertEquals(1f, anim.getFrameTime(0), EPSILON);
        assertEquals(1f, anim.getFrameTime(1), EPSILON);
        assertEquals(2, anim.getDuration(), EPSILON);
    }

    @Test
    public void testGetFrameAtTime() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");

        Animation anim = new Animation(false);
        anim.addFrame(frameA, 1f);
        anim.addFrame(frameB, 1f);

        assertEquals(frameA, anim.getFrameAtTime(0f));
        assertEquals(frameA, anim.getFrameAtTime(0.5f));
        assertEquals(frameB, anim.getFrameAtTime(1f));
        assertEquals(frameB, anim.getFrameAtTime(1.5f));
        assertEquals(frameB, anim.getFrameAtTime(2f));
        assertEquals(frameB, anim.getFrameAtTime(10f));
    }

    @Test
    public void testLoop() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");

        Animation anim = new Animation(true);
        anim.addFrame(frameA, 1f);
        anim.addFrame(frameB, 1f);

        assertEquals(frameA, anim.getFrameAtTime(0f));
        assertEquals(frameA, anim.getFrameAtTime(0.5f));
        assertEquals(frameB, anim.getFrameAtTime(1f));
        assertEquals(frameB, anim.getFrameAtTime(1.5f));
        assertEquals(frameA, anim.getFrameAtTime(2f));
        assertEquals(frameB, anim.getFrameAtTime(3f));
    }

    @Test
    public void testSingleFrameAnimation() {
        MockImage frameA = new MockImage("A");
        Animation anim = new Animation(frameA);

        assertEquals(0f, anim.getDuration(), EPSILON);
        assertEquals(frameA, anim.getFrameAtTime(0f));
        assertEquals(frameA, anim.getFrameAtTime(0.5f));
        assertEquals(frameA, anim.getFrameAtTime(2f));
    }

    @Test
    public void testDifferentFrameDuration() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        MockImage frameC = new MockImage("C");

        Animation anim = new Animation(false);
        anim.addFrame(frameA, 1f);
        anim.addFrame(frameB, 0.5f);
        anim.addFrame(frameC, 1f);

        assertEquals(frameA, anim.getFrameAtTime(0f));
        assertEquals(frameA, anim.getFrameAtTime(0.5f));
        assertEquals(frameB, anim.getFrameAtTime(1f));
        assertEquals(frameC, anim.getFrameAtTime(1.5f));
        assertEquals(frameC, anim.getFrameAtTime(2f));
    }

    @Test
    public void testSkipZeroFrames() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        MockImage frameC = new MockImage("C");

        Animation anim = new Animation(false);
        anim.addFrame(frameA, 1f);
        anim.addFrame(frameB, 0f);
        anim.addFrame(frameC, 1f);

        assertEquals(frameA, anim.getFrameAtTime(0f));
        assertEquals(frameA, anim.getFrameAtTime(0.5f));
        assertEquals(frameC, anim.getFrameAtTime(1f));
    }
}
