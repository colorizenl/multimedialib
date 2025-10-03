//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.scene.Timer;
import org.jspecify.annotations.Nullable;

/**
 * Base interface for all graphics that can be added to the stage. Depending
 * on the renderer, the stage can contain both 2D and 3D graphics, which are
 * represented by {@link StageNode2D} and {@link StageNode3D}, respectively.
 */
public interface StageNode {

    /**
     * Called by the renderer at the end of frame updates, while rendering the
     * stage. {@code animationTimer} contains the elapsed time since the
     * currently active scene was started. This allows animations to display
     * correctly, without the need to update every single node during each
     * frame update.
     */
    public void animate(Timer animationTimer);

    /**
     * Returns this node's parent node. Returns {@code null} if thos node does
     * not have a parent, or if this is the root node.
     */
    public @Nullable StageNode getParent();

    /**
     * Detaches this node from its parent, removing it from the stage. Does
     * nothing if this node does not have a parent.
     */
    public void detach();
}
