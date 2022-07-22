//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExtrapolationTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void noDataAvailable() {
        Extrapolation extrapolation = new Extrapolation(10);

        assertEquals(0f, extrapolation.extrapolate(), EPSILON);
    }

    @Test
    public void singleDataPoint() {
        Extrapolation extrapolation = new Extrapolation(10);
        extrapolation.add(2f);

        assertEquals(2f, extrapolation.extrapolate(), EPSILON);
    }

    @Test
    public void flatLine() {
        Extrapolation extrapolation = new Extrapolation(10);
        extrapolation.add(2f);
        extrapolation.add(2f);
        extrapolation.add(2f);

        assertEquals(2f, extrapolation.extrapolate(), EPSILON);
    }

    @Test
    public void linearExtrapolation() {
        Extrapolation extrapolation = new Extrapolation(10);
        extrapolation.add(2f);
        extrapolation.add(3f);
        extrapolation.add(4f);

        assertEquals(5f, extrapolation.extrapolate(), EPSILON);
    }

    @Test
    void useRotatingBuffer() {
        Extrapolation extrapolation = new Extrapolation(3);
        extrapolation.add(2f);
        extrapolation.add(3f);
        extrapolation.add(4f);
        extrapolation.add(3f);
        extrapolation.add(2f);

        assertEquals(1f, extrapolation.extrapolate(), EPSILON);
    }
}
