package nl.colorize.multimedialib.stage;

import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadStatusTest {

    @Test
    void trackSubscribable() {
        Subject<String> operation = new Subject<>();
        LoadStatus loadStatus = LoadStatus.track(new ResourceFile("a.png"), operation);

        assertFalse(loadStatus.isLoaded());
        operation.next("a");
        assertTrue(loadStatus.isLoaded());
        operation.next("a");
        assertTrue(loadStatus.isLoaded());
    }
}
