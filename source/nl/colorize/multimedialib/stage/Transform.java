//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.MathUtils;

/**
 * Transformation that can be applied to a graphic when displaying it. The
 * transform consists of the following aspects:
 * 
 * <ul>
 *   <li>Rotation (in degrees)</li>
 *   <li>Scale (as a percentage, 100% being the original size)</li>
 *   <li>Alpha (as a percentage, 100% is opaque, 0% is fully transparent)</li>
 *   <li>Flip horizontally</li>
 *   <li>Flip vertically</li>
 *   <li>Mask color</li>
 * </ul>
 */
public class Transform {

    private float rotation;
    private float scaleX;
    private float scaleY;
    private float alpha;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private ColorRGB mask;

    public Transform() {
        reset();
    }

    public void reset() {
        rotation = 0f;
        scaleX = 100f;
        scaleY = 100f;
        flipHorizontal = false;
        flipVertical = false;
        alpha = 100f;
    }

    /**
     * Replaces all values within this transform, overwriting them with the
     * values from the provided other transform.
     */
    public void set(Transform other) {
        rotation = other.rotation;
        scaleX = other.scaleX;
        scaleY = other.scaleY;
        alpha = other.alpha;
        mask = other.mask;
    }

    public void setRotation(float degrees) {
        this.rotation = degrees % (degrees >= 0f ? 360f : -360f);
    }

    public void addRotation(float degrees) {
        setRotation(getRotation() + degrees);
    }
    
    public float getRotation() {
        return rotation;
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

    public float getAlpha() {
        return alpha;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }

    public void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    public boolean isFlipVertical() {
        return flipVertical;
    }

    public void setMask(ColorRGB mask) {
        this.mask = mask;
    }

    public ColorRGB getMask() {
        return mask;
    }

    public Transform copy() {
        Transform other = new Transform();
        other.rotation = rotation;
        other.scaleX = scaleX;
        other.scaleY = scaleY;
        other.alpha = alpha;
        other.flipHorizontal = flipHorizontal;
        other.flipVertical = flipVertical;
        other.mask = mask;
        return other;
    }

    /**
     * Convenience method that creates a new transform with the specified
     * rotation, but all other properties set to their default values.
     */
    public static Transform withRotation(float rotation) {
        Transform transform = new Transform();
        transform.setRotation(rotation);
        return transform;
    }

    /**
     * Convenience method that creates a new transform with the specified scale,
     * but all other properties set to their default values.
     */
    public static Transform withScale(float scaleX, float scaleY) {
        Transform transform = new Transform();
        transform.setScaleX(scaleX);
        transform.setScaleY(scaleY);
        return transform;
    }

    /**
     * Convenience method that creates a new transform with the specified scale,
     * but all other properties set to their default values.
     */
    public static Transform withScale(float scale) {
        return withScale(scale, scale);
    }

    /**
     * Convenience method that creates a new transform with the specified alpha
     * value, but all other properties set to their default values.
     */
    public static Transform withAlpha(float alpha) {
        Transform transform = new Transform();
        transform.setAlpha(alpha);
        return transform;
    }

    public static Transform withMask(ColorRGB mask) {
        Transform transform = new Transform();
        transform.setMask(mask);
        return transform;
    }
}
