//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;

/**
 * Describes how 2D graphics should be displayed, using a number of
 * properties. The following properties are available:
 * <p>
 * <pre>
 * | Property        | Description                                                                |
 * |-----------------|----------------------------------------------------------------------------|
 * | Visible         | When set to false, the graphic will not be displayed at all.               |
 * | Position        | X/Y coordinates of where the graphic's center is displayed on the canvas.  |
 * | Alpha           | Percentage, where 0% is fully transparent and 100% is fully opaque.        |
 * </pre>
 * <p>
 * This class contains the "base" properties that are available to all
 * graphics types. See {@link ImageTransform} for additional properties
 * that are available when displaying images.
 */
@Getter
@Setter
public class Transform {

    private boolean visible;
    private Point2D position;
    private double alpha;

    public Transform() {
        this.visible = true;
        this.position = new Point2D(0f, 0f);
        this.alpha = 100f;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public void setPosition(double x, double y) {
        setPosition(new Point2D(x, y));
    }

    public void setX(double x) {
        setPosition(new Point2D(x, position.y()));
    }

    public void setY(double y) {
        setPosition(new Point2D(position.x(), y));
    }

    public void addPosition(double deltaX, double deltaY) {
        setPosition(position.add(deltaX, deltaY));
    }

    public double getX() {
        return position.x();
    }

    public double getY() {
        return position.y();
    }

    public void setAlpha(double alpha) {
        this.alpha = Math.clamp(alpha, 0f, 100f);
    }

    public void set(Transform other) {
        setVisible(other.visible);
        setPosition(other.position.x(), other.position.y());
        setAlpha(other.alpha);
    }

    /**
     * Returns a new {@link Transform} instance that is the result of combining
     * this transform with the specified other transform.
     */
    public Transform combine(Transform other) {
        if (other instanceof ImageTransform) {
            ImageTransform imageTransform = new ImageTransform();
            imageTransform.set(this);
            return imageTransform.combine(other);
        }

        Transform combined = new Transform();
        combined.setVisible(visible && other.visible);
        combined.setPosition(position.add(other.position));
        combined.setAlpha(multiplyPercentage(alpha, other.alpha));
        return combined;
    }

    @Deprecated
    protected static double multiplyPercentage(double percentageA, double percentageB) {
        return ((percentageA / 100f) * (percentageB / 100f)) * 100f;
    }
}
