//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Circle;
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
public class Primitive implements StageNode2D {

    private Transform transform;
    private Transform globalTransform;

    private Shape shape;
    private ColorRGB color;
    private float stroke;

    public Primitive(Shape shape, ColorRGB color) {
        this.transform = new Transform();
        this.globalTransform = new Transform();

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
        return shape.reposition(globalTransform.getPosition()).getBoundingBox();
    }

    @Override
    public void animate(Timer sceneTime) {
    }

    @Override
    public String toString() {
        if (shape instanceof Rect || shape instanceof Circle) {
            return shape.getClass().getSimpleName() + " [" + shape + "]";
        } else {
            return shape.getClass().getSimpleName();
        }
    }
}
