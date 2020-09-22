//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.action;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionTest {

    @Test
    void callbackOnFrame() {
        List<String> buffer = new ArrayList<>();
        Action action = Action.indefinitely(() -> buffer.add("test"));
        action.update(1f);
        action.update(1f);

        assertEquals(2, buffer.size());
    }

    @Test
    void callbackOnComplete() {
        List<String> buffer = new ArrayList<>();
        Action action = Action.delay(Action.once(() -> buffer.add("test")), 2f);
        action.update(1f);
        action.update(1f);
        action.update(1f);

        assertEquals(1, buffer.size());
    }

    @Test
    void activeForTime() {
        Action action = Action.timed(() -> {}, 2f);

        assertFalse(action.isCompleted());
        action.update(1f);
        assertFalse(action.isCompleted());
        action.update(1f);
        assertTrue(action.isCompleted());
        action.update(1f);
        assertTrue(action.isCompleted());
    }

    @Test
    void chainWithOtherAction() {
        List<String> buffer = new ArrayList<>();

        Action action = Action.chain(Action.timed(() -> buffer.add("1"), 2f),
            Action.indefinitely(() -> buffer.add("2")));

        action.update(1f);
        action.update(1f);
        action.update(1f);
        action.update(1f);

        assertEquals("[1, 1, 2, 2]", buffer.toString());
    }
    
    @Test
    void delay() {
        List<String> buffer = new ArrayList<>();

        Action action = Action.delay(Action.timed(() -> buffer.add("1"), 2f), 2f);
        action.update(1f);
        action.update(1f);
        action.update(1f);
        action.update(1f);
        action.update(1f);

        assertEquals("[1, 1]", buffer.toString());
    }

    @Test
    void instantAction() {
        List<String> buffer = new ArrayList<>();

        Action action = Action.once(() -> buffer.add("ok"));
        action.update(0f);

        assertEquals("[ok]", buffer.toString());
    }

    @Test
    void multipleChains() {
        List<String> buffer = new ArrayList<>();

        Action action1 = Action.once(() -> buffer.add("1"));
        Action action2 = Action.once(() -> buffer.add("2"));
        Action action3 = Action.once(() -> buffer.add("3"));

        Action chain = Action.chain(Action.chain(action1, action2), action3);
        chain.update(1f);
        chain.update(1f);
        chain.update(1f);

        assertEquals("[1, 2, 3]", buffer.toString());
    }
}
