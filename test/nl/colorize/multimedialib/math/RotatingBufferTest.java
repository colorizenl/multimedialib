package nl.colorize.multimedialib.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class RotatingBufferTest {

    @Test
    public void testGetAverageValue() {
        RotatingBuffer buffer = new RotatingBuffer(3);
        buffer.add(1f);
        buffer.add(5f);

        assertEquals(3f, buffer.getAverageValue(), 0.001f);
    }

    @Test
    public void testRotateValues() {
        RotatingBuffer buffer = new RotatingBuffer(3);
        buffer.add(1f);
        buffer.add(5f);
        buffer.add(10f);
        buffer.add(15f);
        buffer.add(20f);

        assertEquals(15f, buffer.getAverageValue(), 0.001f);
    }
}
