//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.scene.Timer;

/**
 * Draws a graphical primitive to the screen. The on-screen position of the
 * primitive is determined by both the coordinates in the original shape and
 * by the primitive's position.
 * <p>
 * {@link Primitive}s have a stroke property, but its value is only used if
 * the instance describes an outline shape. The stroke property has no effect
 * for filled shapes.
 */
@Getter
@Setter
public class Primitive implements Graphic2D {

    private final DisplayListLocation location;
    private Shape shape;
    private ColorRGB color;
    private float stroke;

    public Primitive(Shape shape, ColorRGB color) {
        this.location = new DisplayListLocation(this);
        this.shape = shape;
        this.color = color;
        this.stroke = 1f;
    }

    public Primitive(Shape shape, ColorRGB color, float alpha) {
        this(shape, color);
        getTransform().setAlpha(alpha);
    }

    @Override
    public Rect getStageBounds() {
        Transform globalTransform = getGlobalTransform();
        return shape.reposition(globalTransform.getPosition()).getBoundingBox();
    }

    @Override
    public void updateGraphics(Timer sceneTime) {
    }

    @Override
    public String toString() {
        return shape.getClass().getSimpleName();
    }
}
