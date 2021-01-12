package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.scene.effect.Timer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

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
