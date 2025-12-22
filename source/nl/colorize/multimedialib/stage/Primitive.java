//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.scene.Timer;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PROTECTED;

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

    @Setter(PROTECTED) private Container parent;
    private final Transform transform;
    private final Transform globalTransform;

    @Getter(NONE) private Shape shape;
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

    /**
     * Returns the "raw" {@link Shape} that was used to create this primitive.
     * This might differ from this primitive's <em>current</em> appearance on,
     * the stage, which can be obtained using {@link #getStageShape()}.
     */
    public Shape getRawShape() {
        return shape;
    }

    /**
     * Returns a {@link Shape} that represents this primitive's current
     * appearance on the stage, based on {@link #getGlobalTransform()}.
     * This is different from the "raw" shape that was used to create this
     * primitive, which can be obtained by using {@link #getRawShape()}.
     */
    public Shape getStageShape() {
        return shape.reposition(getGlobalTransform().getPosition());
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

    public static Primitive fromRect(float width, float height, ColorRGB color) {
        return fromRect(width, height, color, 100f);
    }

    public static Primitive fromRect(float width, float height, ColorRGB color, float alpha) {
        return new Primitive(Rect.aroundOrigin(width, height), color, alpha);
    }

    public static Primitive fromCircle(float radius, ColorRGB color) {
        return fromCircle(radius, color, 100f);
    }

    public static Primitive fromCircle(float radius, ColorRGB color, float alpha) {
        return new Primitive(new Circle(radius), color, alpha);
    }
}
