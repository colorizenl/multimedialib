package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RotatingBufferTest {

    @Test
    void getAverageValue() {
        RotatingBuffer buffer = new RotatingBuffer(3);
        buffer.add(1f);
        buffer.add(5f);

        assertEquals(3f, buffer.getAverageValue(), 0.001f);
    }

    @Test
    void rotateValues() {
        RotatingBuffer buffer = new RotatingBuffer(3);
        buffer.add(1f);
        buffer.add(5f);
        buffer.add(10f);
        buffer.add(15f);
        buffer.add(20f);

        assertEquals(15f, buffer.getAverageValue(), 0.001f);
    }

    @Test
    void ignoreOutliers() {
        RotatingBuffer buffer = new RotatingBuffer(3, 600);
        buffer.add(1000f);
        buffer.add(400f);
        buffer.add(500f);

        assertEquals(450f, buffer.getAverageValue(), 0.001f);
    }
}
