//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.scene.Timer;
import org.jspecify.annotations.Nullable;

/**
 * Base interface for all 2D graphics that can be added to the stage's
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
 */
public interface Spatial2D extends Spatial<Transform> {

    /**
     * Returns this node's parent node. Returns {@code null} if thos node does
     * not have a parent, or if this is the root node.
     */
    public @Nullable Container getParent();

    /**
     * Detaches this node from its parent, removing it from the stage. Does
     * nothing if this node does not have a parent.
     */
    default void detach() {
        if (getParent() != null) {
            getParent().removeChild(this);
        }
    }

    /**
     * Returns the bounding box currently occopied by this node, relative to
     * the stage. This method uses {@link #getGlobalTransform()}.
     */
    public Rect getStageBounds();

    /**
     * Convenience method of changing this graphic's position. The X and Y
     * coordinates are relative to the graphic's local transform.
     *
     * @deprecated Prefer using {@code getTransform().setPosition(x, y)}.
     */
    @Deprecated
    default void setPosition(double x, double y) {
        getTransform().setPosition(x, y);
    }
}
