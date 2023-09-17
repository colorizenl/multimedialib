//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.util.stats.Tuple;
import nl.colorize.util.stats.TupleList;

import java.util.function.BiPredicate;

/**
 * Finite state machine that allows a number of possible states, but can
 * only have one currently active state at any point in time. The finite
 * state machine implements the {@link Updatable} interface, so it needs
 * to receive frame updates in order to function.
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

    @Getter private S activeState;
    @Getter private Timer activeStateTimer;
    private S nextState;
    private BiPredicate<S, S> allowedTransitions;

    public StateMachine(S initialState) {
        this.activeState = initialState;
        this.activeStateTimer = Timer.infinite();
        this.nextState = null;
        this.allowedTransitions = (a, b) -> true;
    }

    private boolean isTransitionAllowed(S requestedState) {
        return !activeState.equals(requestedState) &&
            allowedTransitions.test(activeState, requestedState);
    }

    /**
     * Requests to change the currently active state. The new state will remain
     * active until it is explicitly changed.
     * <p>
     * Returns true if the requested state change was accepted. Returns false
     * if the transition from the current state to the new state is rejected.
     */
    public boolean changeState(S requestedState) {
        Preconditions.checkArgument(requestedState != null, "Requested state cannot be null");

        if (!isTransitionAllowed(requestedState)) {
            return false;
        }

        activeState = requestedState;
        activeStateTimer = Timer.infinite();
        nextState = null;
        return true;
    }

    /**
     * Requests to change the currently active state. The new state will only
     * remain active for a limited duration (in seconds), after which the
     * active state will transition back into the previously active state.
     * <p>
     * Returns true if the requested state change was accepted. Returns false
     * if the transition from the current state to the new state is rejected.
     */
    public boolean changeState(S requestedState, float duration) {
        return changeState(requestedState, duration, activeState);
    }

    /**
     * Requests to change the currently active state. The new state will only
     * remain active for a limited duration (in seconds), after which the
     * active state will transition into the requested next state.
     * <p>
     * Returns true if the requested state change was accepted. Returns false
     * if the transition from the current state to the new state is rejected.
     */
    public boolean changeState(S requestedState, float duration, S next) {
        Preconditions.checkArgument(requestedState != null, "Requested state cannot be null");
        Preconditions.checkArgument(next != null, "Next state cannot be null");

        if (!isTransitionAllowed(requestedState)) {
            return false;
        }

        activeState = requestedState;
        activeStateTimer = new Timer(duration);
        nextState = next;
        return true;
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

    @Override
    public void update(float deltaTime) {
        if (activeState instanceof Updatable updatableState) {
            updatableState.update(deltaTime);
        }

        activeStateTimer.update(deltaTime);

        if (activeStateTimer.isCompleted() && nextState != null) {
            changeState(nextState);
        }
    }
}
