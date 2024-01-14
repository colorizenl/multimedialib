//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;

/**
 * Defines the list of transformations that should be applied to graphics
 * when displaying them on the {@link Stage}. The table below shows all
 * available transformation properties, along with the graphics types that
 * support them:
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
 * {@link Transform} instances can be used standalone, but they can also be
 * linked to a parent transform. In the latter case, the term "local transform"
 * refers to this {@link Transform} instance, and "global transform" refers to
 * the combination of this {@link Transform} and its parent (which in turn
 * might also have a parent, and so on).
 */
@Getter
@Setter
public final class Transform {

    private boolean visible;
    private Point2D position;
    private float rotation;
    private float scaleX;
    private float scaleY;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private float alpha;
    private ColorRGB maskColor;

    private Transform parent;

    public Transform() {
        this.visible = true;
        this.position = new Point2D(0f, 0f);
        this.rotation = 0f;
        this.scaleX = 100f;
        this.scaleY = 100f;
        this.flipHorizontal = false;
        this.flipVertical = false;
        this.alpha = 100f;
        this.maskColor = null;
    }

    public void setPosition(float x, float y) {
        position = new Point2D(x, y);
    }

    public void setX(float x) {
        position = new Point2D(x, position.getY());
    }

    public void setY(float y) {
        position = new Point2D(position.getX(), y);
    }

    public void addPosition(float deltaX, float deltaY) {
        position = new Point2D(position.getX() + deltaX, position.getY() + deltaY);
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public void setRotation(float degrees) {
        this.rotation = degrees % (degrees >= 0f ? 360f : -360f);
    }

    public void addRotation(float degrees) {
        setRotation(getRotation() + degrees);
    }

    public float getRotationInRadians() {
        return (float) Math.toRadians(rotation);
    }

    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    public void setScale(float scaleX, float scaleY) {
        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    public void setScaleX(float scaleX) {
        this.scaleX = Math.max(scaleX, 0f);
    }
    
    public float getScaleX() {
        return flipHorizontal ? -scaleX : scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = Math.max(scaleY, 0f);
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
        setPosition(other.position.getX(), other.position.getY());
        setRotation(other.rotation);
        setScale(other.scaleX, other.scaleY);
        setFlipHorizontal(other.flipHorizontal);
        setFlipVertical(other.flipVertical);
        setAlpha(other.alpha);
        setMaskColor(other.maskColor);
    }

    /**
     * Returns a {@link Transform} that represents the combination of this
     * {@link Transform}'s properties with the properties of its parent
     * transform. If the parent transform <em>also</em> has a parent, this
     * logic will be applied recursively.
     */
    public Transform toGlobalTransform() {
        if (parent == null) {
            return this;
        }
        return combine(parent.toGlobalTransform());
    }

    private Transform combine(Transform other) {
        Transform global = new Transform();
        global.setVisible(visible && other.visible);
        global.setPosition(position.move(other.position));
        global.setRotation(rotation + other.rotation);
        global.setScale(combinePercentage(scaleX, other.scaleX),
            combinePercentage(scaleY, other.scaleY));
        global.setFlipHorizontal(flipHorizontal || other.flipHorizontal);
        global.setFlipVertical(flipVertical || other.flipVertical);
        global.setAlpha(combinePercentage(alpha, other.alpha));
        global.setMaskColor(maskColor != null ? maskColor : other.maskColor);
        return global;
    }

    private float combinePercentage(float value, float parentValue) {
        return ((value / 100f) * (parentValue / 100f)) * 100f;
    }
}
