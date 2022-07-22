//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import static org.junit.jupiter.api.Assertions.*;

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
    
    @Test
    void append() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        MockImage frameC = new MockImage("C");
        MockImage frameD = new MockImage("D");
        
        Animation anim1 = new Animation(ImmutableList.of(frameA, frameB), 0.1f, false);
        Animation anim2 = new Animation(ImmutableList.of(frameC, frameD), 0.2f, false);
        Animation result = anim1.append(anim2);
        
        assertEquals(4, result.getFrameCount());
        assertEquals("A", result.getFrameAtIndex(0).toString());
        assertEquals("B", result.getFrameAtIndex(1).toString());
        assertEquals("C", result.getFrameAtIndex(2).toString());
        assertEquals("D", result.getFrameAtIndex(3).toString());
    }
    
    @Test
    void reverse() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        
        Animation anim = new Animation(ImmutableList.of(frameA, frameB), 0.1f, false);
        anim.setFrameTime(1, 0.2f);
        
        Animation reversed = anim.reverse();
        
        assertEquals(2, reversed.getFrameCount());
        assertEquals("B", reversed.getFrameAtIndex(0).toString());
        assertEquals("A", reversed.getFrameAtIndex(1).toString());
        assertEquals(0.2f, reversed.getFrameTime(0), EPSILON);
        assertEquals(0.1f, reversed.getFrameTime(1), EPSILON);
    }
    
    @Test
    void repeat() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        
        Animation anim = new Animation(ImmutableList.of(frameA, frameB), 0.1f, false);
        anim.setFrameTime(1, 0.2f);
        
        Animation repeating = anim.repeat(2);
        
        assertEquals(4, repeating.getFrameCount());
        assertEquals("A", repeating.getFrameAtIndex(0).toString());
        assertEquals("B", repeating.getFrameAtIndex(1).toString());
        assertEquals("A", repeating.getFrameAtIndex(2).toString());
        assertEquals("B", repeating.getFrameAtIndex(3).toString());
        assertEquals(0.1f, repeating.getFrameTime(0), EPSILON);
        assertEquals(0.2f, repeating.getFrameTime(1), EPSILON);
        assertEquals(0.1f, repeating.getFrameTime(2), EPSILON);
        assertEquals(0.2f, repeating.getFrameTime(3), EPSILON);
    }
    
    @Test
    void mirror() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        
        Animation anim = new Animation(ImmutableList.of(frameA, frameB), 0.1f, false);
        anim.setFrameTime(1, 0.2f);
        
        Animation mirrored = anim.mirror();
        
        assertEquals(4, mirrored.getFrameCount());
        assertEquals("A", mirrored.getFrameAtIndex(0).toString());
        assertEquals("B", mirrored.getFrameAtIndex(1).toString());
        assertEquals("B", mirrored.getFrameAtIndex(2).toString());
        assertEquals("A", mirrored.getFrameAtIndex(3).toString());
        assertEquals(0.1f, mirrored.getFrameTime(0), EPSILON);
        assertEquals(0.2f, mirrored.getFrameTime(1), EPSILON);
        assertEquals(0.2f, mirrored.getFrameTime(2), EPSILON);
        assertEquals(0.1f, mirrored.getFrameTime(3), EPSILON);
    }
}
