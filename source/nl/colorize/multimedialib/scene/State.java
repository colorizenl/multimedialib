//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;

/**
 * One of the possible states for a {@link FiniteStateMachine}.
 * <p>
 * By default, states remain active until a new state is explicitly requested
 * via the finite state machine itself. However, it is also possible to define
 * states that are active for a limited duration (in seconds). After this
 * duration has been exceeded, the finite state machine will change state to
 * the next requested state.
 *
 * @param <T> Type of the state properties. This object can be used to
 *            associate additional data with each state.
 */
public record State<T>(String name, float duration, State<T> nextState, T properties) {

    public State {
        Preconditions.checkArgument(!name.isEmpty(), "Missing state name");
        Preconditions.checkArgument(duration >= 0f, "Invalid duration: " + duration);
        Preconditions.checkArgument(duration > 0f == (nextState != null),
            "Cannot define next state for state with infinite duration");
    }

    public static <T> State<T> of(String name, float duration, State<T> nextState, T properties) {
        return new State<>(name, duration, nextState, properties);
    }

    public static <T> State<T> of(String name, T properties) {
        return new State<>(name, 0f, null, properties);
    }
}
