//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Stores information relevant for one of the states. States themselves are
 * intended to be immutable, though the state machine itself obviously has
 * state.
 * <p>
 * This interface can be used in two ways. The first is to implement the
 * interface in custom state classes or enums, and store all configuration
 * related to those states in the class. The second approach is to use the
 * {@link SimpleState} implementation to avoid having to create state classes.
 */
public interface State {

    public String getName();

    public float getDuration();

    public State getNext();

    public boolean isInterruptable();
}
