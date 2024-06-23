package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.util.Subscribable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadStatusTest {

    @Test
    void trackSubscribable() {
        Subscribable<String> operation = new Subscribable<>();
        LoadStatus loadStatus = LoadStatus.track(new FilePointer("a.png"), operation);

        assertFalse(loadStatus.isLoaded());
        operation.next("a");
        assertTrue(loadStatus.isLoaded());
        operation.next("a");
        assertTrue(loadStatus.isLoaded());
    }
}
