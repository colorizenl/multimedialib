//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Point2D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FiniteStateMachineTest {

    @Test
    void changeState() {
        FiniteStateMachine<String> stateMachine = new FiniteStateMachine<>();
        stateMachine.register(State.of("a", "a"));
        stateMachine.register(State.of("b", "b"));

        assertEquals("a", stateMachine.getActiveStateName());
        stateMachine.changeState("b");
        assertEquals("b", stateMachine.getActiveStateName());
    }

    @Test
    void changingToSameStateDoesNothing() {
        FiniteStateMachine<String> stateMachine = new FiniteStateMachine<>();
        stateMachine.register(State.of("a", "a"));
        stateMachine.register(State.of("b", "b"));

        assertEquals(0f, stateMachine.getActiveStateTime(), EPSILON);
        stateMachine.update(1f);
        assertEquals(1f, stateMachine.getActiveStateTime(), EPSILON);
        stateMachine.changeState("a");
        stateMachine.update(1f);
        assertEquals("a", stateMachine.getActiveStateName());
        assertEquals(2f, stateMachine.getActiveStateTime(), EPSILON);
        stateMachine.changeState("b");
        assertEquals("b", stateMachine.getActiveStateName());
        assertEquals(0f, stateMachine.getActiveStateTime(), EPSILON);
    }

    @Test
    void changeStateAfterDuration() {
        State<String> a = State.of("a", "a");
        State<String> b = State.of("b", 2f, a, "b");

        FiniteStateMachine<String> stateMachine = new FiniteStateMachine<>();
        stateMachine.register(a);
        stateMachine.register(b);
        stateMachine.changeState(b);

        assertEquals("b", stateMachine.getActiveStateName());
        stateMachine.update(1f);
        assertEquals("b", stateMachine.getActiveStateName());
        stateMachine.update(1f);
        assertEquals("a", stateMachine.getActiveStateName());
        assertEquals(0f, stateMachine.getActiveStateTime(), EPSILON);
    }
}
