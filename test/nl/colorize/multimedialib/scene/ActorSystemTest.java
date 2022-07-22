//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorSystemTest {

    @Test
    void timed() {
        List<String> buffer = new ArrayList<>();
        ActorSystem system = ActorSystem.timed(2f, () -> buffer.add("."));

        system.update(null, 1f);
        assertEquals(1, buffer.size());
        assertFalse(system.isCompleted());

        system.update(null, 1f);
        assertEquals(2, buffer.size());
        assertTrue(system.isCompleted());
    }

    @Test
    void delay() {
        List<String> buffer = new ArrayList<>();
        ActorSystem system = ActorSystem.delay(2f, () -> buffer.add("."));

        system.update(null, 1f);
        assertEquals(0, buffer.size());
        assertFalse(system.isCompleted());

        system.update(null, 1f);
        assertEquals(1, buffer.size());
        assertTrue(system.isCompleted());
    }

    @Test
    void fromTimeline() {
        List<String> buffer = new ArrayList<>();
        ActorSystem system = ActorSystem.timed(2f, () -> buffer.add("."));

        system.update(null, 1f);
        assertEquals(1, buffer.size());
        assertFalse(system.isCompleted());
    }

    @Test
    void sequence() {
        List<String> buffer = new ArrayList<>();
        ActorSystem system = ActorSystem.sequence(
            ActorSystem.timed(2f, () -> buffer.add("1")),
            ActorSystem.timed(2f, () -> buffer.add("2"))
        );

        system.update(null, 1f);
        assertEquals(List.of("1"), buffer);
        assertFalse(system.isCompleted());

        system.update(null, 1f);
        assertEquals(List.of("1", "1"), buffer);
        assertFalse(system.isCompleted());

        system.update(null, 1f);
        assertEquals(List.of("1", "1", "2"), buffer);
        assertFalse(system.isCompleted());

        system.update(null, 1f);
        assertEquals(List.of("1", "1", "2", "2"), buffer);
        assertTrue(system.isCompleted());
    }
}
