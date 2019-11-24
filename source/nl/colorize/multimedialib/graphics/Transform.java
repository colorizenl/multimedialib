//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

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

    private int rotation;
    private int scaleX;
    private int scaleY;
    private int alpha;
    private ColorRGB mask;
    
    public Transform() {
        reset();
    }

    public void reset() {
        rotation = 0;
        scaleX = 100;
        scaleY = 100;
        alpha = 100;
        mask = null;
    }
    
    /**
     * Returns true if all of this transform's properties are set to their
     * original/default values.
     */
    public boolean isDefaultTransform() {
        return !isRotated() && !isScaled() && alpha == 100 && mask == null;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation % (rotation >= 0 ? 360 : -360);
    }
    
    public int getRotation() {
        return rotation;
    }
    
    public float getRotationInRadians() {
        return (float) Math.toRadians(rotation);
    }
    
    public boolean isRotated() {
        return rotation != 0;
    }

    public void setScale(int scaleX, int scaleY) {
        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    public void setScaleX(int scaleX) {
        Preconditions.checkArgument(scaleX >= 1, "Invalid scale: " + scaleX);
        this.scaleX = scaleX;
    }
    
    public int getScaleX() {
        return scaleX;
    }

    public void setScaleY(int scaleY) {
        Preconditions.checkArgument(scaleY >= 1, "Invalid scale: " + scaleY);
        this.scaleY = scaleY;
    }

    public int getScaleY() {
        return scaleY;
    }

    public boolean isScaled() {
        return scaleX != 100 || scaleY != 100;
    }

    public void setAlpha(int alpha) {
        Preconditions.checkArgument(alpha >= 0 && alpha <= 100, "Invalid alpha: " + alpha);
        this.alpha = alpha;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    public void setMask(ColorRGB mask) {
        this.mask = mask;
    }

    public ColorRGB getMask() {
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Transform) {
            Transform other = (Transform) o;
            return rotation == other.rotation &&
                scaleX == other.scaleX &&
                scaleY == other.scaleY &&
                alpha == other.alpha &&
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
    public static Transform withRotation(int rotation) {
        Transform transform = new Transform();
        transform.setRotation(rotation);
        return transform;
    }

    /**
     * Convenience method that creates a new transform with the specified scale,
     * but all other properties set to their default values.
     */
    public static Transform withScale(int scaleX, int scaleY) {
        Transform transform = new Transform();
        transform.setScaleX(scaleX);
        transform.setScaleY(scaleY);
        return transform;
    }

    /**
     * Convenience method that creates a new transform with the specified alpha
     * value, but all other properties set to their default values.
     */
    public static Transform withAlpha(int alpha) {
        Transform transform = new Transform();
        transform.setAlpha(alpha);
        return transform;
    }

    /**
     * Convenience method that creates a new transform with the specified color
     * mask, but all other properties set to their default values.
     */
    public static Transform withMask(ColorRGB mask) {
        Transform transform = new Transform();
        transform.setMask(mask);
        return transform;
    }
}
