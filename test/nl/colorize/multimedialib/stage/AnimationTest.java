//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnimationTest {

    private static final double EPSILON = 0.001;

    @Test
    public void testCreateRegularAnimation() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");

        Animation anim = new Animation(List.of(frameA, frameB), 1.0, false);

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

        Animation anim = new Animation(List.of(frameA, frameB), 1.0, false);

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

        Animation anim = new Animation(List.of(frameA, frameB), 1.0, true);

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

        Animation anim = new Animation(List.of(frameA, frameB, frameC), List.of(1.0, 0.5, 1.0), false);

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

        Animation anim = new Animation(List.of(frameA, frameB, frameC), List.of(1.0, 0.0, 1.0), false);

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
        
        Animation anim1 = new Animation(List.of(frameA, frameB), 0.1f, false);
        Animation anim2 = new Animation(List.of(frameC, frameD), 0.2f, false);
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
        
        Animation anim = new Animation(List.of(frameA, frameB), List.of(0.1, 0.2), false);
        Animation reversed = anim.reverse();
        
        assertEquals(2, reversed.getFrameCount());
        assertEquals("B", reversed.getFrameAtIndex(0).toString());
        assertEquals("A", reversed.getFrameAtIndex(1).toString());
        assertEquals(0.2, reversed.getFrameTime(0), EPSILON);
        assertEquals(0.1, reversed.getFrameTime(1), EPSILON);
    }
    
    @Test
    void repeat() {
        MockImage frameA = new MockImage("A");
        MockImage frameB = new MockImage("B");
        
        Animation anim = new Animation(List.of(frameA, frameB), List.of(0.1, 0.2), false);
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
        
        Animation anim = new Animation(List.of(frameA, frameB), List.of(0.1, 0.2), false);
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
