//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import java.util.Collections;

/**
 * Actors are used to structure scene logic, with each actor being responsible
 * for a certain functional scope. Only one scene can be active at the same
 * time, but a scene can consist of many actors. Since actors have a smaller
 * functional scope, they (unlike their parent scene) will not automatically
 * receive access to the full {@link SceneContext}. The actor will receive
 * frame updates for as long as its parent scene remains active.
 *
 * @see SceneContext#attach(Actor)
 */
@FunctionalInterface
public interface Actor {

    /**
     * Called by the renderer during frame updates for as long as this actor
     * is active. Each call to this method results in this actor simulating
     * one "time slice" of scene logic.
     *
     * @param deltaTime Elapsed time since the last frame update, in seconds.
     */
    public void update(double deltaTime);

    /**
     * Actors can override this to return true when they no longer wish to
     * receive frame updates. If this returns false, actors will continue
     * to receive frame updates for the duration of their parent scene.
     * The default implementation always returns false.
     */
    default boolean isCompleted() {
        return false;
    }

    /**
     * Returns a list of sub-actors that should be updated in conjunction
     * with this actor. This allows the sub-actors to receive frame updates
     * without needing to first individually attach each sub-actor to the
     * parent scene. The sub-actors will receive frame updates for as long
     * as the parent scene is active <em>and</em> this actor is active. This
     * actor will receive frame updates before its sub-actors. The default
     * implementation returns an empty list.
     */
    default Iterable<Actor> getSubActors() {
        return Collections.emptyList();
    }
}
