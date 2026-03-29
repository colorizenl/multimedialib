//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
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
    void distanceTo() {
        assertEquals(0f, new Angle(90f).distanceTo(new Angle(90f)), EPSILON);
        assertEquals(90f, new Angle(90f).distanceTo(new Angle(180f)), EPSILON);
        assertEquals(180f, new Angle(90f).distanceTo(new Angle(270f)), EPSILON);
        assertEquals(150f, new Angle(90f).distanceTo(new Angle(300f)), EPSILON);
    }

    @Test
    void angleTo() {
        Angle north = new Angle(0);
        Angle east = new Angle(90);
        Angle south = new Angle(180);
        Angle west = new Angle(270);

        assertEquals(0f, north.angleTo(north), EPSILON);
        assertEquals(90f, north.angleTo(east), EPSILON);
        assertEquals(180f, north.angleTo(south), EPSILON);
        assertEquals(-90f, north.angleTo(west), EPSILON);

        assertEquals(-90f, east.angleTo(north), EPSILON);
        assertEquals(0f, east.angleTo(east), EPSILON);
        assertEquals(90f, east.angleTo(south), EPSILON);
        assertEquals(180f, east.angleTo(west), EPSILON);

        assertEquals(180f, south.angleTo(north), EPSILON);
        assertEquals(-90f, south.angleTo(east), EPSILON);
        assertEquals(0f, south.angleTo(south), EPSILON);
        assertEquals(90f, south.angleTo(west), EPSILON);

        assertEquals(90f, west.angleTo(north), EPSILON);
        assertEquals(180f, west.angleTo(east), EPSILON);
        assertEquals(-90f, west.angleTo(south), EPSILON);
        assertEquals(0f, west.angleTo(west), EPSILON);
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

    @Test
    void fromRadians() {
        assertEquals("0°", Angle.fromRadians(0f).toString());
        assertEquals("90°", Angle.fromRadians((float) Math.PI / 2f).toString());
        assertEquals("180°", Angle.fromRadians((float) Math.PI).toString());
    }

    @Test
    void constants() {
        assertEquals("[0°, 90°, 180°, 270°]", Angle.CARDINAL.toString());
        assertEquals("[0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°]", Angle.INTERCARDINAL.toString());
    }
}
