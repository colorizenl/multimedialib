//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Data;
import nl.colorize.multimedialib.math.MathUtils;
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
 */
@Data
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

    public void addPosition(float deltaX, float deltaY) {
        position = new Point2D(position.getX() + deltaX, position.getY() + deltaY);
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
        this.alpha = MathUtils.clamp(alpha, 0f, 100f);
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
     * Combines this transform with the specified parent transform, and returns
     * the result as a new {@link Transform} instance.
     */
    public Transform combine(Transform parent) {
        Transform global = new Transform();
        global.setVisible(visible || parent.visible);
        global.setPosition(position.getX() + parent.position.getX(),
            position.getY() + parent.position.getY());
        global.setRotation(rotation + parent.rotation);
        global.setScale(combinePercentage(scaleX, parent.scaleX),
            combinePercentage(scaleY, parent.scaleY));
        global.setFlipHorizontal(flipHorizontal || parent.flipHorizontal);
        global.setFlipVertical(flipVertical || parent.flipVertical);
        global.setAlpha(combinePercentage(alpha, parent.alpha));
        global.setMaskColor(maskColor != null ? maskColor : parent.maskColor);
        return global;
    }

    private float combinePercentage(float value, float parentValue) {
        return ((value / 100f) * (parentValue / 100f)) * 100f;
    }
}
