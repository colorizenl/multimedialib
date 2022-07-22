//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateMachineTest {

    @Test
    public void testChangeState() {
        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(SimpleState.create("a"));
        stateMachine.changeState(SimpleState.create("b"));

        assertEquals("b", stateMachine.getActiveState().getName());
    }

    @Test
    public void testChangingToSameStateDoesNothing() {
        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(SimpleState.create("a"));

        assertFalse(stateMachine.changeState(SimpleState.create("a")));
        assertTrue(stateMachine.changeState(SimpleState.create("b")));
    }

    @Test
    public void testFiniteState() {
        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(SimpleState.timed("a", 2f, SimpleState.create("b")));

        assertEquals("a", stateMachine.getActiveStateName());
        stateMachine.update(1f);
        assertEquals("a", stateMachine.getActiveStateName());
        stateMachine.update(1f);
        assertEquals("b", stateMachine.getActiveStateName());
        stateMachine.update(1f);
        assertEquals("b", stateMachine.getActiveStateName());
    }

    @Test
    public void testCannotInterruptNonInterruptable() {
        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(SimpleState.permanent("a"));
        stateMachine.changeState(SimpleState.create("b"));

        assertEquals("a", stateMachine.getActiveStateName());
    }

    @Test
    public void testOverruleStateTime() {
        SimpleState a = SimpleState.create("a");
        SimpleState b = SimpleState.timed("b", 1f, a);

        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(b, 2f);
        stateMachine.update(1f);
        assertEquals("b", stateMachine.getActiveStateName());
        stateMachine.update(1f);
        assertEquals("a", stateMachine.getActiveStateName());
    }
    
    @Test
    void uniterruptableStateShouldStillChangeToNext() {
        SimpleState b = SimpleState.permanent("b");
        SimpleState a = SimpleState.uninterruptable("a", 10f, b);
        
        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(a);
        
        stateMachine.update(5f);
        assertEquals("a", stateMachine.getActiveStateName());
        stateMachine.update(5f);
        assertEquals("b", stateMachine.getActiveStateName());
        stateMachine.update(5f);
        assertEquals("b", stateMachine.getActiveStateName());
    }

    @Test
    void previousState() {
        SimpleState x = SimpleState.permanent("x", true);
        SimpleState a = SimpleState.timed("a", 2f, x);
        SimpleState b = SimpleState.timed("b", 2f, a);

        StateMachine<SimpleState> stateMachine = new StateMachine<>();
        stateMachine.changeState(a);

        assertEquals(a, stateMachine.getActiveState());
        assertNull(stateMachine.getPreviousState());
        stateMachine.update(1f);
        stateMachine.update(1f);
        assertEquals(x, stateMachine.getActiveState());
        assertEquals(a, stateMachine.getPreviousState());

        stateMachine.changeState(b);

        stateMachine.update(1f);
        assertEquals(b, stateMachine.getActiveState());
        assertEquals(x, stateMachine.getPreviousState());
        stateMachine.update(1f);
        assertEquals(a, stateMachine.getActiveState());
        assertEquals(b, stateMachine.getPreviousState());
    }
}
