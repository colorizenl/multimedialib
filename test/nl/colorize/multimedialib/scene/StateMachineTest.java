//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.util.stats.Tuple;
import nl.colorize.util.stats.TupleList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateMachineTest {

    @Test
    void changeState() {
        StateMachine<String> stateMachine = new StateMachine<>("a");
        assertEquals("a", stateMachine.getActiveState());
        stateMachine.requestState("b");
        assertEquals("b", stateMachine.getActiveState());
    }

    @Test
    void restrictTransitions() {
        StateMachine<String> stateMachine = new StateMachine<>("a");
        stateMachine.allowTransitions(TupleList.of(Tuple.of("a", "b")));

        assertTrue(stateMachine.requestState("b"));
        assertEquals("b", stateMachine.getActiveState());
        assertFalse(stateMachine.requestState("c"));
        assertFalse(stateMachine.requestState("c", 2f));
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
        stateMachine.requestState(b);
        stateMachine.update(1f);
        stateMachine.requestState(a);
        stateMachine.update(1f);

        assertEquals("[a, a, b, a]", frames.toString());
    }

    @Test
    void changeStateAfterDuration() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.requestState(b, 2f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, a, a]", frames.toString());
    }

    @Test
    void revertStateAfterDuration() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.requestState(b, 2f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, a, a]", frames.toString());
    }

    @Test
    void queueStateImmediatelyChangesPermanentState() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");
        Updatable c = deltaTime -> frames.add("c");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.requestState(b);
        stateMachine.requestState(c);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, c, c]", frames.toString());
    }

    @Test
    void queueStateWaitsForTemporaryStateToCompleteBeforeChanging() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");
        Updatable c = deltaTime -> frames.add("c");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.requestState(b, 2f);
        stateMachine.requestState(c);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, c]", frames.toString());
    }

    @Test
    void forceState() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");
        Updatable c = deltaTime -> frames.add("c");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.update(1f);
        stateMachine.requestState(b);
        stateMachine.forceState(c);
        stateMachine.update(1f);

        assertEquals("[a, c]", frames.toString());
    }

    @Test
    void retainState() {
        List<String> frames = new ArrayList<>();
        Updatable a = deltaTime -> frames.add("a");
        Updatable b = deltaTime -> frames.add("b");

        StateMachine<Updatable> stateMachine = new StateMachine<>(a);
        stateMachine.requestState(b);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[b, b, b]", frames.toString());
    }

    @Test
    void updateStateTime() {
        StateMachine<String> stateMachine = new StateMachine<>("a");
        stateMachine.requestState("b");
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals(2f, stateMachine.getActiveStateTimer().getTime(), EPSILON);
    }

    @Test
    void updateDefaultStateTime() {
        StateMachine<String> stateMachine = new StateMachine<>("a");
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals(2f, stateMachine.getActiveStateTimer().getTime(), EPSILON);
    }
}
