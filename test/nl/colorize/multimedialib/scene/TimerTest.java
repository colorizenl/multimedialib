package nl.colorize.multimedialib.scene;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TimerTest {

    @Test
    public void testTrackTime() {
        Timer timer = new Timer(2f);
        assertFalse(timer.isCompleted());
        timer.update(1.5f);
        assertFalse(timer.isCompleted());
        timer.update(1.5f);
        assertTrue(timer.isCompleted());
    }

    @Test
    public void testAttachActions() {
        AtomicInteger count = new AtomicInteger();

        Timer timer = new Timer(2f);
        timer.attach(() -> count.addAndGet(1));
        timer.update(1f);
        timer.update(1f);
        timer.update(1f);

        assertEquals(1, count.get());
    }
}