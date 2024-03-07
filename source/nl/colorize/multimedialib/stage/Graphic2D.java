//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Updatable;

/**
 * Shared interface for all types of 2D graphics that are part of the scene
 * graph. It defines a common API for managing graphics. Each graphic is part
 * of the <em>display list</em>, which is controlled by the renderer and
 * determines when and how graphics should be displayed.
 */
public interface Graphic2D extends Updatable {

    /**
     * Returns the {@link DisplayListLocation} attached to this graphic, which
     * is used by the renderer to determine how this graphic should be drawn.
     */
    public DisplayListLocation getLocation();

    /**
     * Provides access to this graphic's <em>local</em> transform, which can
     * be used to influence how the graphic should be displayed. As the
     * "local" implies, these properties are relative to the graphic's parent.
     * The graphic's <em>global</em> transform is then calculated by combining
     * the graphic's transform properties with those of its parent.
     */
    default Transform getTransform() {
        return getLocation().getLocalTransform();
    }

    /**
     * Returns this graphic's <em>global</em> transform, which is calculated
     * by combining the graphic's <em>local</em> transform with that of its
     * parents.
     */
    default Transformable getGlobalTransform() {
        return getLocation().getGlobalTransform();
    }

    /**
     * Convenience method of changing this graphic's position. The X and Y
     * coordinates are relative to the graphic's local transform.
     *
     * @deprecated Prefer {@code getTransform().setPosition(x, y)}.
     */
    @Deprecated
    default void setPosition(float x, float y) {
        getTransform().setPosition(x, y);
    }

    /**
     * Returns the smallest possible rectangle that can contain this graphic,
     * based on its current position and size. The returned coordinates are
     * relative to the stage, <em>not</em> relative to the graphic's parent.
     */
    public Rect getStageBounds();

    /**
     * Returns true if the specified stage coordinates are included within
     * this graphic's bounds. The default implementation does not perform
     * a pixel-perfect check, and instead relies on the graphic's bounding
     * rectangle as returned by {@link #getStageBounds()}.
     */
    default boolean hitTest(Point2D p) {
        return getStageBounds().contains(p);
    }
}
