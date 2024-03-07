//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import nl.colorize.multimedialib.math.Angle;
import nl.colorize.multimedialib.math.Point2D;

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
 * {@link Transform} instances represent a <em>local</em> transforms, with
 * their properties interpreted relative to their parent. In contrast,
 * <em>global</em> transforms are interpreted relative to the stage. Changing
 * a property in a local transform will automatically propagate to the
 * attached global transform.
 */
@Getter
public final class Transform implements Transformable {

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

    public void setVisible(boolean visible) {
        this.visible = visible;
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

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0f, 100f);
    }

    public void setMaskColor(ColorRGB maskColor) {
        this.maskColor = maskColor;
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
     * Updates this {@link Transform}, by combining all properties with those
     * of the specified other {@link Transform}.
     */
    public void combine(Transform other) {
        setVisible(visible && other.visible);
        setPosition(position.move(other.position));
        setRotation(rotation.degrees() + other.rotation.degrees());
        setScaleX(combinePercentage(scaleX, other.scaleX));
        setScaleY(combinePercentage(scaleY, other.scaleY));
        setFlipHorizontal(flipHorizontal || other.flipHorizontal);
        setFlipVertical(flipVertical || other.flipVertical);
        setAlpha(combinePercentage(alpha, other.alpha));
        setMaskColor(maskColor != null ? maskColor : other.maskColor);
    }

    private float combinePercentage(float value, float otherValue) {
        float normalizedValue = value / 100f;
        float normalizedOtherValue = otherValue / 100f;
        return (normalizedValue * normalizedOtherValue) * 100f;
    }
}
