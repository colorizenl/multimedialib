//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimerTest {

    @Test
    public void testTrackTime() {
        Timer timer = new Timer(2f);

        assertEquals(0f, timer.getTime(), EPSILON);
        assertFalse(timer.isCompleted());

        timer.update(1.5f);

        assertEquals(1.5f, timer.getTime(), EPSILON);
        assertFalse(timer.isCompleted());

        timer.update(1.5f);

        assertEquals(2.0f, timer.getTime(), EPSILON);
        assertTrue(timer.isCompleted());

        timer.update(1f);

        assertEquals(2.0f, timer.getTime(), EPSILON);
        assertTrue(timer.isCompleted());
    }

    @Test
    void limitTime() {
        Timer timer = new Timer(10f);

        timer.setTime(-1f);
        assertEquals(0f, timer.getTime(), EPSILON);

        timer.setTime(3f);
        assertEquals(3f, timer.getTime(), EPSILON);

        timer.setTime(12f);
        assertEquals(10f, timer.getTime(), EPSILON);
    }

    @Test
    void at() {
        assertEquals("0.0 / 2.0", Timer.at(0f, 2f).toString());
        assertEquals("1.0 / 2.0", Timer.at(1f, 2f).toString());
        assertEquals("2.0 / 2.0", Timer.at(2f, 2f).toString());
        assertEquals("2.0", Timer.at(2f).toString());
    }

    @Test
    void ratioForZeroTimer() {
        Timer zeroTimer = Timer.none();

        assertEquals(0f, zeroTimer.getRatio(), EPSILON);
        assertTrue(zeroTimer.isCompleted());
    }
}
