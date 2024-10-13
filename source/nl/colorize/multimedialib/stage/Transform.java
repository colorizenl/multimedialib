//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Angle;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.util.stats.Aggregate;

/**
 * Defines the list of transformation properties that should be applied
 * to graphics when displaying them. The table below shows all available
 * properties, along with the graphics types that support them:
 * <p>
 * <pre>
 * | Property          | Defined as                                 | Supported by       |
 * |-------------------|--------------------------------------------|--------------------|
 * | Visible           | true/false                                 | All graphics types |
 * | Position          | X/Y relative to the graphic's center       | All graphics types |
 * | Rotation          | Degrees, clockwise                         | Sprite             |
 * | Scale             | Percentage, 100% indicates original size   | Sprite             |
 * | Flip horizontally | true/false                                 | Sprite             |
 * | Flip vertically   | true/false                                 | Sprite             |
 * | Alpha             | Percentage, 100% indicates opaque          | Sprite, Primitive  |
 * | Mask color        | Replaces non-transparent pixels with color | Sprite             |
 * </pre>
 * <p>
 * The properties in a {@link Transform} instance are relative to its location
 * in the scene graph. In other words, it describes a graphic's <em>local</em>
 * transform. The renderer then combines this with the transform of the
 * graphic's parents to calculate the <em>global</em> transform relative to
 * the stage.
 */
@Getter
@Setter
public final class Transform {

    private boolean visible;
    private Point2D position;
    private Angle rotation;
    private float scaleX;
    private float scaleY;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private float alpha;
    private ColorRGB maskColor;

    public Transform() {
        this.visible = true;
        this.position = new Point2D(0f, 0f);
        this.rotation = Angle.ORIGIN;
        this.scaleX = 100f;
        this.scaleY = 100f;
        this.flipHorizontal = false;
        this.flipVertical = false;
        this.alpha = 100f;
        this.maskColor = null;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        setPosition(new Point2D(x, y));
    }

    public void setX(float x) {
        setPosition(new Point2D(x, position.y()));
    }

    public void setY(float y) {
        setPosition(new Point2D(position.x(), y));
    }

    public void addPosition(float deltaX, float deltaY) {
        setPosition(position.move(deltaX, deltaY));
    }

    public float getX() {
        return position.x();
    }

    public float getY() {
        return position.y();
    }

    public void setRotation(Angle rotation) {
        this.rotation = rotation;
    }

    public void setRotation(float degrees) {
        setRotation(new Angle(degrees));
    }

    public void addRotation(float degrees) {
        setRotation(rotation.move(degrees));
    }

    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    public void setScaleX(float scaleX) {
        this.scaleX = Math.abs(scaleX);
    }
    
    public float getScaleX() {
        return flipHorizontal ? -scaleX : scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = Math.abs(scaleY);
    }

    public float getScaleY() {
        return flipVertical ? -scaleY : scaleY;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0f, 100f);
    }

    /**
     * Replaces all transformation properties in this {@link Transform} with
     * the values from the specified other {@link Transform}.
     */
    public void set(Transform other) {
        setVisible(other.visible);
        setPosition(other.position.x(), other.position.y());
        setRotation(other.rotation.degrees());
        setScaleX(other.scaleX);
        setScaleY(other.scaleY);
        setFlipHorizontal(other.flipHorizontal);
        setFlipVertical(other.flipVertical);
        setAlpha(other.alpha);
        setMaskColor(other.maskColor);
    }

    /**
     * Returns a new {@link Transform} instance that is the result of combining
     * this transform with the specified other transform.
     */
    public Transform combine(Transform other) {
        Transform combined = new Transform();
        combined.setVisible(visible && other.visible);
        combined.setPosition(position.move(other.position));
        combined.setRotation(rotation.degrees() + other.rotation.degrees());
        combined.setScaleX(Aggregate.multiplyPercentage(scaleX, other.scaleX));
        combined.setScaleY(Aggregate.multiplyPercentage(scaleY, other.scaleY));
        combined.setFlipHorizontal(flipHorizontal || other.flipHorizontal);
        combined.setFlipVertical(flipVertical || other.flipVertical);
        combined.setAlpha(Aggregate.multiplyPercentage(alpha, other.alpha));
        combined.setMaskColor(maskColor != null ? maskColor : other.maskColor);
        return combined;
    }
}
