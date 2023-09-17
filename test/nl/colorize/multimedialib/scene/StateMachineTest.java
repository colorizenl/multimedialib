//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.util.stats.Tuple;
import nl.colorize.util.stats.TupleList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateMachineTest {

    @Test
    void changeState() {
        StateMachine<String> stateMachine = new StateMachine<>("a");
        assertEquals("a", stateMachine.getActiveState());
        stateMachine.changeState("b");
        assertEquals("b", stateMachine.getActiveState());
    }

    @Test
    void restrictTransitions() {
        StateMachine<String> stateMachine = new StateMachine<>("a");
        stateMachine.allowTransitions(TupleList.of(Tuple.of("a", "b")));

        assertTrue(stateMachine.changeState("b"));
        assertEquals("b", stateMachine.getActiveState());
        assertFalse(stateMachine.changeState("c"));
        assertEquals("b", stateMachine.getActiveState());
    }

    @Test
    void stateReceivesUpdates() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.changeState(b);
        stateMachine.update(1f);
        stateMachine.changeState(a);
        stateMachine.update(1f);

        assertEquals("[a, a, b, a]", frames.toString());
    }

    @Test
    void changeStateAfterDuration() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");
        Updatable c = deltaTime -> frames.add("c");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.changeState(b, 2f, c);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, c, c]", frames.toString());
    }

    @Test
    void revertStateAfterDuration() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.changeState(b, 2f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, a, a]", frames.toString());
    }
}
