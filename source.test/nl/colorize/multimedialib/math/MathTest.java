//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for classes in the {@link nl.colorize.multimedialib.math} package.
 */
public class MathTest {
    
    private static final float EPSILON = 0.001f;

    @Test
    public void testNextPowerOfTwo() {
        assertEquals(16, MathUtils.nextPowerOfTwo(11));
        assertEquals(16, MathUtils.nextPowerOfTwo(15));
        assertEquals(16, MathUtils.nextPowerOfTwo(16));
        assertEquals(32, MathUtils.nextPowerOfTwo(17));
        assertEquals(128, MathUtils.nextPowerOfTwo(78));
        assertEquals(128, MathUtils.nextPowerOfTwo(128));
    }
    
    @Test
    public void testIsPowerOfTwo() {
        assertTrue(MathUtils.isPowerOfTwo(4));
        assertFalse(MathUtils.isPowerOfTwo(5));
        assertTrue(MathUtils.isPowerOfTwo(16));
    }
    
    @Test
    public void testAspectRatio() {
        assertEquals(1f, MathUtils.getAspectRatio(0, 0), 0.01f);
        assertEquals(1f, MathUtils.getAspectRatio(2, 2), 0.01f);
        assertEquals(1.33f, MathUtils.getAspectRatio(4, 3), 0.01f);
    }
    
    @Test
    public void testSignum() {
        assertEquals(-1, MathUtils.signum(-123));
        assertEquals(0, MathUtils.signum(0));
        assertEquals(1, MathUtils.signum(1));
        
        assertEquals(-1, MathUtils.signum(-123.5f));
        assertEquals(0, MathUtils.signum(0.0f));
        assertEquals(1, MathUtils.signum(0.1f));
    }
    
    @Test
    public void testSum() {
        assertEquals(0, MathUtils.sum(new int[] {}));
        assertEquals(3, MathUtils.sum(new int[] {3}));
        assertEquals(17, MathUtils.sum(new int[] {2, 0, 7, 8}));
    }
    
    @Test
    public void testClamp() {
        assertEquals(2, MathUtils.clamp(2, 0, 10));
        assertEquals(3, MathUtils.clamp(2, 3, 10));
        assertEquals(0, MathUtils.clamp(2, -2, 0));
    }
    
    @Test
    public void testCeiling() {
        assertEquals(3, MathUtils.ceiling(3.0f));
        assertEquals(4, MathUtils.ceiling(3.1f));
        assertEquals(4, MathUtils.ceiling(3.5f));
        assertEquals(4, MathUtils.ceiling(3.9f));
        assertEquals(4, MathUtils.ceiling(4.0f));
    }
    
    @Test
    public void testAverage() {
        assertEquals(Float.NaN, MathUtils.average(ImmutableList.<Float>of()), EPSILON);
        assertEquals(2f, MathUtils.average(ImmutableList.of(2f)), EPSILON);
        assertEquals(2.1f, MathUtils.average(ImmutableList.of(2f, 2.2f)), EPSILON);
        assertEquals(3.733f, MathUtils.average(ImmutableList.of(2f, 2.2f, 7f)), EPSILON);
    }
}
