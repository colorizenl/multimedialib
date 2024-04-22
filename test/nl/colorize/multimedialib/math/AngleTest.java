//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AngleTest {

    @Test
    void normalizeAngle() {
        assertEquals(90f, new Angle(90f).degrees(), EPSILON);
        assertEquals(180f, new Angle(180f).degrees(), EPSILON);
        assertEquals(0f, new Angle(360f).degrees(), EPSILON);
        assertEquals(40f, new Angle(400f).degrees(), EPSILON);
        assertEquals(260f, new Angle(-100f).degrees(), EPSILON);
        assertEquals(320f, new Angle(-400f).degrees(), EPSILON);
    }

    @Test
    void toRadians() {
        assertEquals(0f, new Angle(0f).getRadians(), EPSILON);
        assertEquals(1.570f, new Angle(90f).getRadians(), EPSILON);
        assertEquals(3.141f, new Angle(180f).getRadians(), EPSILON);
        assertEquals(4.712f, new Angle(270f).getRadians(), EPSILON);
    }

    @Test
    void difference() {
        assertEquals(0f, new Angle(90f).difference(new Angle(90f)), EPSILON);
        assertEquals(90f, new Angle(90f).difference(new Angle(180f)), EPSILON);
        assertEquals(180f, new Angle(90f).difference(new Angle(270f)), EPSILON);
        assertEquals(150f, new Angle(90f).difference(new Angle(300f)), EPSILON);
    }

    @Test
    void normalizeEqualAngles() {
        assertEquals(new Angle(0f).degrees(), new Angle(360f).degrees());
        assertEquals(new Angle(180f).degrees(), new Angle(-180f).degrees());
    }

    @Test
    void move() {
        assertEquals(60f, new Angle(320f).move(new Angle(100)).degrees(), EPSILON);
        assertEquals(80f, new Angle(400).move(new Angle(400)).degrees(), EPSILON);
    }

    @Test
    void opposite() {
        assertEquals(180f, new Angle(0f).opposite().degrees(), EPSILON);
        assertEquals(0f, new Angle(180f).opposite().degrees(), EPSILON);
        assertEquals(270f, new Angle(90f).opposite().degrees(), EPSILON);
        assertEquals(90f, new Angle(270f).opposite().degrees(), EPSILON);
    }
}
