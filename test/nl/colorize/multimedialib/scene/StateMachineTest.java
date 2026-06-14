//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateMachineTest {

    @Test
    void changeState() {
        StateMachine<String> stateMachine = StateMachine.withDefaultState("a");
        assertEquals("a", stateMachine.getCurrentState());
        stateMachine.queueState("b");
        assertEquals("b", stateMachine.getCurrentState());
    }

    @Test
    void stateReceivesUpdates() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.queueState(b);
        stateMachine.update(1f);
        stateMachine.queueState(a);
        stateMachine.update(1f);

        assertEquals("[a, a, b, a]", frames.toString());
    }

    @Test
    void changeStateAfterDuration() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.update(1f);
        stateMachine.queueState(b, 2f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, a, a]", frames.toString());
    }

    @Test
    void revertStateAfterDuration() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.update(1f);
        stateMachine.queueState(b, 2f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, a, a]", frames.toString());
    }

    @Test
    void queueStateImmediatelyChangesPermanentState() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");
        Actor c = deltaTime -> frames.add("c");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.update(1f);
        stateMachine.queueState(b);
        stateMachine.queueState(c);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, c, c]", frames.toString());
    }

    @Test
    void queueStateWaitsForTemporaryStateToCompleteBeforeChanging() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");
        Actor c = deltaTime -> frames.add("c");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.update(1f);
        stateMachine.queueState(b, 2f);
        stateMachine.queueState(c);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[a, b, b, c]", frames.toString());
    }

    @Test
    void forceState() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");
        Actor c = deltaTime -> frames.add("c");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.update(1f);
        stateMachine.queueState(b);
        stateMachine.changeState(c);
        stateMachine.update(1f);

        assertEquals("[a, c]", frames.toString());
    }

    @Test
    void retainState() {
        List<String> frames = new ArrayList<>();
        Actor a = deltaTime -> frames.add("a");
        Actor b = deltaTime -> frames.add("b");

        StateMachine<Actor> stateMachine = StateMachine.withDefaultState(a);
        stateMachine.queueState(b);
        stateMachine.update(1f);
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals("[b, b, b]", frames.toString());
    }

    @Test
    void updateStateTime() {
        StateMachine<String> stateMachine = StateMachine.withDefaultState("a");
        stateMachine.queueState("b");
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals(2f, stateMachine.getCurrentStateTimer().getTime(), EPSILON);
    }

    @Test
    void updateDefaultStateTime() {
        StateMachine<String> stateMachine = StateMachine.withDefaultState("a");
        stateMachine.update(1f);
        stateMachine.update(1f);

        assertEquals(2f, stateMachine.getCurrentStateTimer().getTime(), EPSILON);
    }

    @Test
    void stayInStateIfThereIsNoDefaultState() {
        List<String> framesA = new ArrayList<>();
        List<String> framesB = new ArrayList<>();

        StateMachine<Actor> stateMachineA = StateMachine.withDefaultState(_ -> framesA.add("a"));
        stateMachineA.changeState(_ -> framesA.add("b"), 2f);
        stateMachineA.update(1f);
        stateMachineA.update(1f);
        stateMachineA.update(1f);

        StateMachine<Actor> stateMachineB = StateMachine.withInitialState(_ -> framesB.add("a"));
        stateMachineB.changeState(_ -> framesB.add("b"), 2f);
        stateMachineB.update(1f);
        stateMachineB.update(1f);
        stateMachineB.update(1f);

        assertEquals("[b, b, a]", framesA.toString());
        assertEquals("[b, b, b]", framesB.toString());
    }
}
