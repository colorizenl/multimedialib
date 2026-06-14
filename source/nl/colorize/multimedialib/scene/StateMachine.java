//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Finite state machine that can switch its currently active state, which
 * defines its behavior. The state machine implements the {@link Actor}
 * interface and is therefore part of the current scene. Frame updates are
 * forwarded to the currently active state. States remain active until
 * changed or for a preconfigured amount of time. States can also be queued.
 * <p>
 * State machines can be created with or without a defaut state. If there
 * is a default state, the state machine will automatically switch to the
 * default state when the current state is completed and there are no more
 * states in the queue. If there is no default state, the state machine will
 * remain in the current state until a new state is queued.
 * <p>
 * {@link StateMachine} instances should be encapsulated by the object for
 * which the state applies. The state machine itself does not have any
 * knowledge on when it is appropriate to change the state.
 *
 * @param <S> The type of state represented by this finite state machine.
 *            Two objects are considered "the same state" based on
 *            {@link Object#equals(Object)}. If the state implements the
 *            {@link Actor} interface, it will receive frame updates
 *            when it is active.
 */
public class StateMachine<S> implements Actor {

    @Getter private S currentState;
    @Getter private Timer currentStateTimer;
    private Deque<RequestedState<S>> requestedStates;
    private S defaultState;

    private StateMachine(S initialState, @Nullable S defaultState) {
        this.currentState = initialState;
        this.currentStateTimer = Timer.infinite();
        this.requestedStates = new ArrayDeque<>();
        this.defaultState = defaultState;
    }

    /**
     * Changes this state machine to the specified state, immediately changing
     * to the requested state regardless of the currently active state. The new
     * state will remain active indefinitely.
     */
    public void changeState(S state) {
        changeState(state, Double.MAX_VALUE);
    }

    /**
     * Changes this state machine to the specified state, immediately changing
     * to the requested state regardless of the currently active state. The new
     * state will remain active for the specified duration (in seconds).
     */
    public void changeState(S state, double duration) {
        Preconditions.checkArgument(duration >= 0.0, "Invalid duration: " + duration);

        if (currentState.equals(state)) {
            return;
        }

        currentState = state;
        currentStateTimer = new Timer(duration);
        requestedStates.clear();
    }

    /**
     * Requests the state machine to change to the specified state, but only
     * after the currently active state has been completed. The new state will
     * remain active indefinitely.
     */
    public void queueState(S state) {
        queueState(state, Double.MAX_VALUE);
    }

    /**
     * Requests the state machine to change to the specified state, but only
     * after the currently active state has been completed. The new state will
     * remain active for the specified duration (in seconds).
     */
    public void queueState(S state, double duration) {
        Preconditions.checkArgument(duration >= 0.0, "Invalid duration: " + duration);

        if (currentState.equals(state)) {
            return;
        }

        if (currentStateTimer.isInfinite() || currentStateTimer.isCompleted()) {
            changeState(state, duration);
        } else {
            requestedStates.push(new RequestedState<>(state, duration));
        }
    }

    @Override
    public void update(double deltaTime) {
        if (currentState instanceof Actor stateActor) {
            stateActor.update(deltaTime);
        }

        currentStateTimer.update(deltaTime);

        if (currentStateTimer.isCompleted()) {
            if (!requestedStates.isEmpty()) {
                RequestedState<S> nextState = requestedStates.pop();
                changeState(nextState.state, nextState.duration);
            } else if (defaultState != null) {
                changeState(defaultState);
            }
        }
    }

    /**
     * Creates a new state machine where {@code state} acts as both the
     * initial state and as the default state.
     */
    public static <S> StateMachine<S> withDefaultState(S state) {
        return new StateMachine<>(state, state);
    }

    /**
     * Creates a new state machine where {@code state} acts as the initial
     * state, but there is no default state.
     */
    public static <S> StateMachine<S> withInitialState(S state) {
        return new StateMachine<>(state, null);
    }

    /**
     * Keeps track of the configuration for a requested state that is in
     * the queue but is not active yet.
     */
    private record RequestedState<S>(S state, double duration) {
    }
}
