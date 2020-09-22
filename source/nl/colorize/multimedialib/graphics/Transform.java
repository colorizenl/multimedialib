//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.MathUtils;

/**
 * Transformation that can be applied to a graphic when displaying it. The
 * transform consists of the following aspects:
 * 
 * <ul>
 *   <li>Rotation (in degrees)
 *   <li>Scale (as a percentage, 100% being the original size)
 *   <li>Alpha (as a percentage, 100% is opaque, 0% is fully transparent)
 * </ul>
 */
public class Transform implements AlphaTransform {

    private float rotation;
    private float scaleX;
    private float scaleY;
    private float alpha;
    private ColorRGB mask;

    private static final Transform DEFAULT_TRANSFORM = new Transform();

    public Transform() {
        reset();
    }

    public void reset() {
        rotation = 0f;
        scaleX = 100f;
        scaleY = 100f;
        alpha = 100f;
    }
    
    /**
     * Returns true if all of this transform's properties are set to their
     * original/default values.
     */
    public boolean isDefaultTransform() {
        return equals(DEFAULT_TRANSFORM);
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
        return scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = Math.max(scaleY, 0f);
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setAlpha(float alpha) {
        Preconditions.checkArgument(alpha >= 0f && alpha <= 100f,
            "Alpha value out of range 0-100: " + alpha);

        this.alpha = alpha;
    }

    @Override
    public float getAlpha() {
        return alpha;
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
        other.mask = mask;
        return other;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Transform) {
            Transform other = (Transform) o;
            return MathUtils.equals(rotation, other.rotation) &&
                MathUtils.equals(scaleX, other.scaleX) &&
                MathUtils.equals(scaleY, other.scaleY) &&
                MathUtils.equals(alpha, other.alpha) &&
                Objects.equal(mask, other.mask);
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(rotation, scaleX, scaleY, alpha, mask);
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
