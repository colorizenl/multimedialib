//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Scene logic can be placed in the scene itself, or (in larger scenes) divided
 * among a number of agents. Agents are active for a certain period of time, or
 * until the end of the scene, but they cannot outlive the current scene.
 * In comparison to scenes, agents have a smaller scope and can therefore not
 * access the entire scene context. Finally, agents do not have a special
 * method for startup logic, since they are assumed to be initialized by their
 * parent scene.
 */
public interface Agent extends Updatable {

    /**
     * Indicates this agent has completed and should be removed from the current
     * scene. Even if this never returns true, the agent will still be stopped
     * once its parent scene ends.
     */
    default boolean isCompleted() {
        return false;
    }
}
