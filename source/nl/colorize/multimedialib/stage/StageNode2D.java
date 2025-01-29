//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Timer;

/**
 * Shared interface for all 3D graphics that can be added to the stage's
 * <a href="https://en.wikipedia.org/wiki/Scene_graph">scene graph</a>.
 * The renderer draws all graphics on the stage after each frame update by
 * visiting all nodes.
 * <p>
 * Nodes have two different "transforms", which determines how and where
 * they are drawn by the renderer. The <em>local</em> transform is interpreted
 * relative to its parent node. Combining a node's local transform with the
 * transform of its parent results in its <em>global</em> transform, which is
 * interpreted relative to the stage.
 * <p>
 * Nodes do not implement the {@link nl.colorize.multimedialib.scene.Updatable}
 * interface. Nodes are not expected to be updated <em>every</em> frame: The
 * renderer will generally not draw graphics that are not currently visible,
 * for performance reasons. Therefore, instead of relying on frame updates,
 * the renderer will use {@link #animate(Timer)} to update currently visible
 * nodes while rendering the stage.
 * <p>
 * Although the stage can contain both 2D and 3D graphics, it has a hard
 * separation between the two. Refer to {@link StageNode3D} for the equivalent
 * interface for 2D graphics.
 */
public interface StageNode2D {

    /**
     * Called by the renderer at the end of frame updates, while rendering the
     * stage. {@code animationTimer} contains the elapsed time since the
     * currently active scene was started. This allows animations to display
     * correctly, without the need to update every single node during each
     * frame update.
     */
    public void animate(Timer animationTimer);

    /**
     * Returns this node's local transform, which is interpreted relative to
     * its parent node. The local transform can be modified by application
     * code to modify how and where the node is displayed.
     */
    public Transform getTransform();

    /**
     * Returns this node's global transform, which is interpreted relative to
     * the stage. The global transform is updated by the renderer when drawing
     * the stage at the end of frame updates. Recalculating the global
     * transform is relatively expensive, and is therefore only done once per
     * frame. This means that any changes made to a node's local transform
     * since the last frame update may not yet been reflected in the current
     * state of its global transform.
     */
    public Transform getGlobalTransform();

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
    default void setPosition(float x, float y) {
        getTransform().setPosition(x, y);
    }
}
