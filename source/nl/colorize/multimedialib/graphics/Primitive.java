//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;

/**
 * Draws a graphical primitive to the screen. The on-screen position of the
 * primitive is determined by both the coordinates in the original shape and
 * by the primitive's position.
 */
public class Primitive implements Graphic2D {

    private Shape shape;
    private ColorRGB color;
    private Point2D position;
    private float alpha;
    private boolean visible;

    public static final int TYPE_LINE = 1;
    public static final int TYPE_RECT = 2;
    public static final int TYPE_CIRCLE = 3;
    public static final int TYPE_POLYGON = 4;

    private Primitive(Shape shape, ColorRGB color) {
        this.shape = shape;
        this.color = color;
        this.position = new Point2D(0f, 0f);
        this.alpha = 100f;
        this.visible = true;
    }

    public Shape getShape() {
        return shape;
    }

    /**
     * Returns an integer constant describing the type of shape, returns one of
     * the {@code TYPE_X} constants.
     */
    public int getShapeType() {
        if (shape instanceof Line) return TYPE_LINE;
        if (shape instanceof Rect) return TYPE_RECT;
        if (shape instanceof Circle) return TYPE_CIRCLE;
        if (shape instanceof Polygon) return TYPE_POLYGON;
        throw new IllegalStateException("Unknown shape: " + shape.getClass());
    }

    public void setColor(ColorRGB color) {
        this.color = color;
    }

    public ColorRGB getColor() {
        return color;
    }

    public void setAlpha(float alpha) {
        this.alpha = MathUtils.clamp(alpha, 0f, 100f);
    }

    public float getAlpha() {
        return alpha;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public Rect getBounds() {
        return shape.reposition(position).getBoundingBox();
    }

    @Override
    public boolean hitTest(Point2D point) {
        return shape.reposition(position).contains(point);
    }

    /**
     * Factory method that creates a {@link Primitive} instance with a type based
     * on the provided shape.
     */
    public static Primitive of(Shape shape, ColorRGB color) {
        return new Primitive(shape, color);
    }

    /**
     * Factory method that creates a {@link Primitive} instance with a type based
     * on the provided shape.
     */
    public static Primitive of(Shape shape, ColorRGB color, float alpha) {
        Primitive result = of(shape, color);
        result.setAlpha(alpha);
        return result;
    }
}
