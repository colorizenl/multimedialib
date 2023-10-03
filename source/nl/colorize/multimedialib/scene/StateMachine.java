//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.util.stats.Tuple;
import nl.colorize.util.stats.TupleList;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiPredicate;

/**
 * Finite state machine that allows a number of possible states, but can
 * only have one currently active state at any point in time.
 * <p>
 * The state machine implements the {@link Updatable} interface, so it needs
 * to receive frame updates in order to function. Requested states are stored
 * in a queue, with the actual state change only occurring during the frame
 * update.
 * <p>
 * By default, every state is allowed to transition into every other state.
 * The state machine can optionally be configured to restrict allowed
 * transitions based on the currently active state.
 *
 * @param <S> The type of state represented by this finite state machine.
 *            States are considered equal if {@code stateA.equals(stateB)}.
 *            If the state implements the {@link Updatable} interface, the
 *            {@code update} method of the currently active state will be
 *            called when the state machine itself receives frame updates.
 */
public class StateMachine<S> implements Updatable {

    private Deque<RequestedState<S>> stateQueue;
    private S defaultState;
    private BiPredicate<S, S> allowedTransitions;

    public StateMachine(S defaultState) {
        this.stateQueue = new LinkedList<>();
        this.defaultState = defaultState;
        this.allowedTransitions = (a, b) -> true;
    }

    /**
     * Requests the state machine to transition into the specified state at
     * the earliest opportunity. Once active, the state will remain active
     * until another state is requested.
     * <p>
     * Returns a boolean indicating if the state machine allows a transition
     * from the preceding state in the queue into the requested state.
     */
    public boolean requestState(S nextState) {
        return requestState(nextState, 0f);
    }

    /**
     * Requests the state machine to transition into the specified state at
     * the earliest opportunity. Once active, the state will remain active
     * for the specified duration (in seconds).
     * <p>
     * Returns a boolean indicating if the state machine allows a transition
     * from the preceding state in the queue into the requested state.
     */
    public boolean requestState(S nextState, float duration) {
        if (!isTransitionAllowed(nextState)) {
            return false;
        }

        boolean interruptible = duration == 0f;
        Timer timer = interruptible ? Timer.infinite() : new Timer(duration);

        RequestedState<S> stateInfo = new RequestedState<>(nextState, timer, interruptible);
        stateQueue.offer(stateInfo);
        return true;
    }

    /**
     * Forces this state machine into the specified state, clearing the queue
     * so that the requested state becomes active during the next frame update.
     */
    public void forceState(S nextState) {
        stateQueue.clear();
        requestState(nextState);
    }

    /**
     * Restricts state transitions based on the specified predicate. The first
     * and second argument in the callback function refer to the current state
     * and requested state respectively.
     */
    public void allowTransitions(BiPredicate<S, S> callback) {
        allowedTransitions = callback;
    }

    /**
     * Restricts state transitions to only those included in the specified
     * list. The first and second argument in each tuple refers to the current
     * state and requested state respectively.
     */
    public void allowTransitions(TupleList<S, S> allowed) {
        Preconditions.checkArgument(!allowed.isEmpty(), "Provided list is empty");
        allowedTransitions = (a, b) -> allowed.contains(Tuple.of(a, b));
    }

    private boolean isTransitionAllowed(S requestedState) {
        if (stateQueue.isEmpty()) {
            return !requestedState.equals(defaultState);
        }

        S precedingState = stateQueue.getLast().state;
        return !requestedState.equals(precedingState) &&
            allowedTransitions.test(precedingState, requestedState);
    }

    @Override
    public void update(float deltaTime) {
        if (isActiveStateCompleted()) {
            stateQueue.pop();
        }

        if (stateQueue.isEmpty()) {
            updateState(defaultState, deltaTime);
            return;
        }

        RequestedState<S> active = stateQueue.peek();
        updateState(active.state, deltaTime);
        active.timer.update(deltaTime);

        // We intentionally check the active state at the start
        // *and* at the end of every frame, just to ensure the
        // state machine is in the expected state at all times.
        if (isActiveStateCompleted()) {
            stateQueue.pop();
        }
    }

    private void updateState(S state, float deltaTime) {
        if (state instanceof Updatable updatableState) {
            updatableState.update(deltaTime);
        }
    }

    public S getActiveState() {
        if (stateQueue.isEmpty()) {
            return defaultState;
        }

        RequestedState<S> active = stateQueue.peek();
        return active.state;
    }

    public Timer getActiveStateTimer() {
        if (stateQueue.isEmpty()) {
            return Timer.infinite();
        }

        RequestedState<S> active = stateQueue.peek();
        return active.timer;
    }

    private boolean isActiveStateCompleted() {
        if (stateQueue.isEmpty()) {
            return false;
        }

        RequestedState<S> active = stateQueue.peek();
        boolean hasNextState = stateQueue.size() >= 2;
        return active.timer.isCompleted() || (active.interruptible && hasNextState);
    }

    /**
     * Data structure indicating one of the states in the queue. Also keeps
     * track of the state's progress while it is active.
     */
    private record RequestedState<S>(S state, Timer timer, boolean interruptible) {
    }
}
