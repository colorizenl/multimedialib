//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.math.Shape;

import java.util.Map;

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

    private final StageLocation location;
    private Shape shape;
    private ColorRGB color;
    private float stroke;

    public static final int TYPE_LINE = 1;
    public static final int TYPE_RECT = 2;
    public static final int TYPE_CIRCLE = 3;
    public static final int TYPE_POLYGON = 4;
    public static final int TYPE_SEGMENTED_LINE = 5;

    private static final Map<Class<? extends Shape>, Integer> TYPE_MAPPING = Map.of(
        Line.class, TYPE_LINE,
        Rect.class, TYPE_RECT,
        Circle.class, TYPE_CIRCLE,
        Polygon.class, TYPE_POLYGON,
        SegmentedLine.class, TYPE_SEGMENTED_LINE
    );

    public Primitive(Shape shape, ColorRGB color) {
        this.location = new StageLocation();
        this.shape = shape;
        this.color = color;
        this.stroke = 1f;
    }

    public Primitive(Shape shape, ColorRGB color, float alpha) {
        this(shape, color);
        getTransform().setAlpha(alpha);
    }

    /**
     * Returns an integer constant describing the type of shape, returns one of
     * the {@code TYPE_X} constants.
     */
    public int getShapeType() {
        Integer type = TYPE_MAPPING.get(shape.getClass());
        Preconditions.checkState(type != null, "Unknown shape: " + shape.getClass());
        return type;
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public Rect getStageBounds() {
        Transform globalTransform = getGlobalTransform();
        return shape.reposition(globalTransform.getPosition()).getBoundingBox();
    }

    @Override
    public String toString() {
        return "Primitive [" + shape.toString() + "]";
    }
}
