//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathUtilsTest {

    @Test
    public void testClamp() {
        assertEquals(2, MathUtils.clamp(2, 0, 10));
        assertEquals(3, MathUtils.clamp(2, 3, 10));
        assertEquals(0, MathUtils.clamp(2, -2, 0));
    }

    @Test
    void numberFormat() {
        assertEquals("1.0", MathUtils.format(1f, 1));
        assertEquals("1.4", MathUtils.format(1.4f, 1));
        assertEquals("1.5", MathUtils.format(1.5f, 1));
        assertEquals("1.6", MathUtils.format(1.55f, 1));
    }
}
