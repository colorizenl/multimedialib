//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.UUID;

/**
 * Shared interface for all types of 2D graphics that are part of the scene
 * graph. It defines a common API for managing graphics, with subclasses
 * adding the specific behavior for controlling the appearance.
 * <p>
 * Graphics have both a <em>local</em> transform, which is relative to the
 * graphic's parent in the scene graph, and a <em>global</em> transform that
 * is relative to the stage.
 */
public interface Graphic2D extends Updatable {

    public StageLocation getLocation();

    default UUID getId() {
        return getLocation().getId();
    }

    default void detach() {
        if (getLocation().getParent() != null) {
            getLocation().getParent().removeChild(this);
        }
    }

    /**
     * Returns this graphic's <em>local</em> transform, which indicates how the
     * graphic should be displayed relative to its parent. The graphic's global
     * transform can be derived from its local transform when necessary.
     */
    default Transform getTransform() {
        return getLocation().getLocalTransform();
    }

    default void setPosition(float x, float y) {
        getTransform().setPosition(x, y);
    }

    default void setPosition(Point2D position) {
        getTransform().setPosition(position);
    }

    /**
     * Returns this graphic's <em>global</em> transform, which indicates how
     * the graphic should be displayed on the stage.
     */
    default Transform getGlobalTransform() {
        return getLocation().getGlobalTransform();
    }

    /**
     * Returns the position and size of this graphic on the stage. For
     * non-rectangular graphics this returns the smallest possible
     * rectangle that contains this graphic.
     */
    public Rect getStageBounds();
}
