//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.multimedialib.scene.Updatable;

/**
 * Shared interface for all types of 2D graphics that are part of the scene
 * graph. It defines a common API for managing graphics. Each graphic is part
 * of the <em>display list</em>, which is controlled by the renderer and
 * determines when and how graphics should be displayed.
 * <p>
 * Graphics do <em>not</em> implement the {@link Updatable} interface. The
 * renderer will avoid rendering non-visible graphics to improve performance,
 * so graphics cannot and should not rely on receiving frame updates every
 * single frame. Instead of the {@link Updatable} interface, animated
 * graphics can be updated using {@link #updateGraphics(Timer)}, which
 * supports intermittent frame updates.
 */
public interface Graphic2D {

    /**
     * Updates this gaphic's state based on the global animation timer. This
     * method is <em>not</em> called during every single frame update, it is
     * only called when the renderer considers this graphic to be visible.
     */
    public void updateGraphics(Timer animationTimer);

    /**
     * Returns this graphic's <em>local</em> transform, which can be modified
     * to influence how the graphic should be displayed. The properties in the
     * local transform should be interpreted relative to the graphic's parent.
     * The renderer will calculate the gaphic's <em>global</em> transform
     * (i.e. relative to the stage) when the graphic is rendered.
     */
    public Transform getTransform();

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

    /**
     * Returns this graphic's parent in the scene graph. Returns {@code null}
     * if this graphic does not have a parent, i.e. if this is the scene graph
     * root container. Also returns {@code null} if this graphic has not yet
     * been added to the scene graph.
     */
    public Graphic2D getParent();

    /**
     * Calculates the graphic's <em>global</em> transform, and returns the
     * resulting {@link Transform} instance. Modifying how graphics are
     * displayed should be done using the <em>local</em> transform, which can
     * be obtained using {@link #getTransform()}. This method can be used when
     * application code needs to know the graphic's transform relative to the
     * stage.
     * <p>
     * <strong>Performance note:</strong> This is a relatively expensive
     * operation. The renderer normally only calculates the global transform
     * while rendering graphics, so calculating the global transform ad-hoc
     * has a performance cost. It's not a problem to calculate the global
     * transform in application code if it's for a few graphics, but try to
     * avoid doing this for hundreds of graphics every single frame.
     */
    default Transform calculateGlobalTransform() {
        Graphic2D parent = getParent();
        Transform localTransform = getTransform();
        if (parent == null) {
            return localTransform;
        }
        return parent.calculateGlobalTransform().combine(localTransform);
    }

    /**
     * Returns the smallest possible rectangle that can contain this graphic,
     * based on its current position and size. The returned coordinates are
     * relative to the stage.
     * <p>
     * <strong>Performance note:</strong> This method needs to calculate the
     * graphic's global transform. See {@link #calculateGlobalTransform()}
     * for more information on the associated performance behavior.
     */
    public Rect getStageBounds();

    /**
     * Convenience method that detaches this graphic from its parent, removing
     * its from the scene graph. If this graphic is not part of the scene graph
     * calling this method does nothing.
     */
    default void detach() {
        Graphic2D parent = getParent();
        if (parent instanceof Container parentContainer) {
            parentContainer.removeChild(this);
        }
    }
}
