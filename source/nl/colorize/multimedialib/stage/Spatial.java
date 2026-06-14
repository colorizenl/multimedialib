//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.scene.Timer;

/**
 * Base interface for all graphics that can be added to the stage's
 * <a href="https://en.wikipedia.org/wiki/Scene_graph">scene graph</a>.
 * <p>
 * Spatial nodes have two different "transforms", which determines how and
 * where they are drawn by the renderer. The <em>local</em> transform is
 * interpreted relative to its parent node. The local transform is then
 * combined with its parent(s) into the <em>global</em> transform, which is
 * interpreted relative to the stage.
 * <p>
 * Spatials are <em>not</em> actors and therefore do not implement the
 * {@link Actor} interface. Unlike actors,
 * spatials not guaranteed to receive frame updates during every frame. For
 * performance reasons, the renderer will only animate and draw graphics that
 * are currently visible. Therefore, instead of relying on frame updates,
 * spatials use {@link #animate(Timer)} to animate graphics while rendering
 * the stage.
 *
 * @param <T> The type of local/global transform that is applied to this
 *            spatial node.
 */
public interface Spatial<T> {

    /**
     * Called by the renderer at the end of each frame update, before drawing
     * this node. The value of {@code animationTimer} represents the elapsed
     * time since the currently active scene was started. Note the renderer
     * will <em>only</em> call this method if this node is actually visible.
     */
    public void animate(Timer animationTimer);

    /**
     * Returns this node's local transform, which is interpreted relative to
     * its parent node. Application code can modify the local transform to
     * influence how and where the node is displayed.
     */
    public T getTransform();

    /**
     * Returns this node's global transform, which is interpreted relative to
     * the stage. The renderer updates the global transform when drawing the
     * stage at the end of each frame update.
     * <p>
     * Recalculating the global transform is relatively expensive and is
     * therefore only done once per frame. This means that any changes made
     * to a node's local transform since the last frame update may not yet be
     * reflected in the current state of its global transform.
     */
    public T getGlobalTransform();
}
