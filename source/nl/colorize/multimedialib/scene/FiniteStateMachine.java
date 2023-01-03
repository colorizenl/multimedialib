//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Finite state machine that allows a number of possible states, but only allows
 * one active state at any point in time.
 * <p>
 * Transitions into a new state can be triggered internally or externally.
 * Internal transitions are initiated by the current state, which can request
 * the new state to be active after a certain period of time. External
 * transitions are started by simply calling a method on the state machine
 * itself.
 *
 * @param <T> Type of the state properties. See {@link State} for more
 *            information on states.
 */
public class FiniteStateMachine<T> implements Updatable {

    private Map<String, State<T>> possibleStates;
    private State<T> activeState;
    private float activeStateTime;

    public FiniteStateMachine() {
        this.possibleStates = new HashMap<>();
        this.activeState = null;
        this.activeStateTime = 0f;
    }

    /**
     * Registers a state as a possible state for this state machine. If this is
     * the first state to be registered, it is automatically set as the active
     * state.
     *
     * @throws IllegalArgumentException if the state machine already contains a
     *         state with the same name.
     */
    public void register(State<T> state) {
        Preconditions.checkArgument(!possibleStates.containsKey(state.name()),
            "Finite state machine already contains state: " + state.name());

        possibleStates.put(state.name(), state);

        if (possibleStates.size() == 1) {
            changeState(state);
        }
    }

    /**
     * Changes this finite state machine's active state. The requested state
     * will remain active until it has either completed, or the active state is
     * changed by calling this method. If the requested state was already active
     * this method does nothing.
     *
     * @throws IllegalArgumentException if the requested state is not registered
     *         as a possible state for this finite state machine.
     */
    public void changeState(State<T> requestedState) {
        Preconditions.checkArgument(requestedState != null, "Cannot use null state");

        if (activeState != null && requestedState.name().equals(activeState.name())) {
            return;
        }

        activeState = requestedState;
        activeStateTime = 0f;
    }

    /**
     * Changes this finite state machine's active state to the state with the
     * specified name. The requested state will remain active until it has either
     * completed, or the active state is changed by calling this method. If the
     * requested state was already active this method does nothing.
     *
     * @throws IllegalArgumentException if the requested state is not registered
     *         as a possible state for this finite state machine.
     */
    public void changeState(String requestedStateName) {
        State<T> requestedState = possibleStates.get(requestedStateName);
        Preconditions.checkArgument(requestedState != null,
            "Not a possible state: " + requestedStateName);
        changeState(requestedState);
    }

    @Override
    public void update(float deltaTime) {
        checkActiveState();

        activeStateTime += deltaTime;

        if (activeState.duration() > 0f && activeStateTime >= activeState.duration()) {
            changeState(activeState.nextState());
        }
    }

    private void checkActiveState() {
        Preconditions.checkState(activeState != null,
            "Finite state machine is not in an active state");
    }

    public void resetActiveState() {
        activeStateTime = 0f;
    }

    public State<T> getActiveState() {
        checkActiveState();
        return activeState;
    }

    public String getActiveStateName() {
        return getActiveState().name();
    }

    public T getActiveStateProperties() {
        return getActiveState().properties();
    }

    public float getActiveStateTime() {
        return activeStateTime;
    }

    public Set<State<T>> getPossibleStates() {
        return ImmutableSet.copyOf(possibleStates.values());
    }

    public State<T> getPossibleState(String name) {
        State<T> state = possibleStates.get(name);
        Preconditions.checkArgument(state != null, "Not a possible state: " + state);
        return state;
    }

    public boolean hasPossibleState(String name) {
        return possibleStates.containsKey(name);
    }
}
