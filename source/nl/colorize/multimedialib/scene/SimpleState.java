//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Minimal implementation of the {@link State} interface that can be used in
 * state machines.
 */
public class SimpleState implements State {

    private String name;
    private float duration;
    private State next;
    private boolean interruptable;

    public SimpleState(String name, float duration, State next, boolean interruptable) {
        this.name = name;
        this.duration = duration;
        this.next = next;
        this.interruptable = interruptable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    public State getNext() {
        return next;
    }

    @Override
    public boolean isInterruptable() {
        return interruptable;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleState) {
            SimpleState other = (SimpleState) o;
            return name.equals(other.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Convenience method to create a simple state that will remain active until
     * it is interrupted by changing to a different state.
     */
    public static SimpleState create(String name) {
        return new SimpleState(name, 0f, null, true);
    }

    /**
     * Convenience method to create a simple state that is only active for a
     * finite amount of time, after which it will change to the specified new
     * state.
     */
    public static SimpleState timed(String name, float duration, State next) {
        return new SimpleState(name, duration, next, true);
    }

    /**
     * Creates a state that is only active for a finite amount of time, and
     * cannot be interrupted within this period.
     */
    public static SimpleState uninterruptable(String name, float duration, State next) {
        return new SimpleState(name, duration, next, false);
    }

    /**
     * Convenience method to create a simple state that will remain active
     * indefinitely, until it is interrupted.
     */
    public static SimpleState permanent(String name, boolean interruptable) {
        return new SimpleState(name, 0f, null, interruptable);
    }

    /**
     * Convenience method to create a simple state that will remain active
     * forever and cannot be interrupted by changing to another state.
     *
     * @deprecated Explicitly define if the permanent can still be interrupted,
     *             use {@link #permanent(String, boolean)} instead.
     */
    @Deprecated
    public static SimpleState permanent(String name) {
        return permanent(name, false);
    }
}
