//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import nl.colorize.multimedialib.renderer.Updatable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a finite state machine. States can be identified by name or by
 * the actual state object. States can have characteristics such as timed
 * versus infinite, and interruptable versus permanent. The behavior of the
 * state machine will depend on those characteristics, of both the current
 * state and the requested new state.
 *
 * @param <T> The type of state which is being controlled by this state
 *            machine.
 */
public class StateMachine<T extends State> implements Updatable {

    private Map<String, T> possibleStates;
    private T activeState;
    private float activeStateTime;
    private float activeStateDuration;
    private T previousState;

    public StateMachine() {
        this.possibleStates = new HashMap<>();
        this.activeState = null;
        this.activeStateTime = 0f;
        this.activeStateDuration = 0f;
        this.previousState = null;
    }

    /**
     * Registers a state as a possible state for this state machine. If this is
     * the first state to be registered, it is automatically set as the active
     * state.
     *
     * @throws IllegalArgumentException if the state has an invalid name, or if
     *         the state machine already contains a state with the same name.
     */
    public void register(T state) {
        String stateName = state.getName();

        Preconditions.checkArgument(stateName.length() > 0,
            "Invalid state name: " + stateName);
        Preconditions.checkArgument(!possibleStates.containsKey(stateName),
            "State machine already contains state with name " + stateName);

        possibleStates.put(stateName, state);

        if (possibleStates.size() == 1) {
            activeState = state;
            activeStateTime = 0f;
            activeStateDuration = state.getDuration();
        }
    }

    /**
     * Changes this state machine to the specified state. If the state is already
     * active, or if the currently active state cannot be interrupted, this
     * method does nothing. Returns true if the state was actually changed as a
     * result of calling this method.
     * <p>
     * This will also register the state with the state machine, if this is not
     * already the case.
     */
    public boolean changeState(T requestedState) {
        float duration = requestedState.getDuration();
        return changeState(requestedState, duration);
    }

    /**
     * Changes this state machine to the specified state, which will remain
     * active for the specified time. The requested duration overrides the
     * duration indicated in the state itself. Once the state has finished,
     * the state machine will switch to the next state. If the next state is
     * {@code null}, the state machine will remain in the requested state
     * indefinitely.
     * <p>
     * If the state is already active, or if the currently active state cannot
     * be interrupted, this method does nothing. Returns true if the state was
     * actually changed as a result of calling this method.
     * <p>
     * This will also register the state with the state machine, if this is not
     * already the case.
     *
     * @throws IllegalArgumentException if the requested state has a limited
     *         duration, but does not specify a next state that should be
     *         activated once it has finished.
     */
    public boolean changeState(T requestedState, float duration) {
        Preconditions.checkArgument(!hasDuration(requestedState) || requestedState.getNext() != null,
            "State with duration does not indicate next state: " + requestedState);

        if (requestedState.equals(activeState) || !canChangeState()) {
            return false;
        }

        previousState = activeState;
        activeState = requestedState;
        activeStateTime = 0f;
        activeStateDuration = duration;

        if (!possibleStates.containsValue(requestedState)) {
            possibleStates.put(requestedState.getName(), requestedState);
        }

        return true;
    }

    private boolean canChangeState() {
        if (activeState == null) {
            return true;
        }

        return activeState.isInterruptable() || isActiveStateCompleted();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(float deltaTime) {
        Preconditions.checkState(activeState != null,
            "State machine does not have an active state");

        activeStateTime += deltaTime;

        if (isActiveStateCompleted()) {
            changeState((T) activeState.getNext());
        }
    }

    private boolean isActiveStateCompleted() {
        return activeStateDuration > 0f && activeStateTime >= activeStateDuration;
    }

    public void resetActiveState() {
        activeStateTime = 0f;
    }

    public T getActiveState() {
        Preconditions.checkState(activeState != null,
            "State machine has not yet been set to an active state");
        return activeState;
    }

    public String getActiveStateName() {
        return getActiveState().getName();
    }

    public float getActiveStateTime() {
        return activeStateTime;
    }

    public float getActiveStateDuration() {
        return activeStateTime;
    }

    public Set<T> getPossibleStates() {
        return ImmutableSet.copyOf(possibleStates.values());
    }

    public T getPossibleState(String name) {
        Preconditions.checkArgument(hasState(name), "Unknown state: " + name);
        return possibleStates.get(name);
    }

    public boolean hasState(String name) {
        return possibleStates.containsKey(name);
    }

    public boolean hasState(State state) {
        return hasState(state.getName());
    }

    private boolean hasDuration(T state) {
        return state.getDuration() > 0f;
    }

    /**
     * Returns the state machine's state that was active before the currently
     * active state. Note that this could return {@code null} if the state
     * machine is currently in its initial state.
     */
    public T getPreviousState() {
        return previousState;
    }
}
