//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

/**
 * Interface that is notified whenever the stage is modified. It can be used
 * to synchronize data structures with the stage. All methods in this
 * interface are optional.
 */
public interface StageSubscriber {

    default void onNodeAdded(Container parent, StageNode2D node) {
    }

    default void onNodeRemoved(Container parent, StageNode2D node) {
    }

    default void onNodeAdded(Group parent, StageNode3D node) {
    }

    default void onNodeRemoved(Group parent, StageNode3D node) {
    }
}
