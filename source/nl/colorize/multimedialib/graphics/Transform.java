//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Objects;

import nl.colorize.multimedialib.math.MathUtils;

/**
 * Describes a transformation that can be applied to a graphic before displaying
 * it. The transform consists of the following aspects:
 * 
 * <ul>
 *   <li>Rotation (in degrees)
 *   <li>Scale (as a percentage, 100% being the original size)
 *   <li>Alpha (as a percentage, 100% is opaque, 0% is fully transparent)
 * </ul>
 */
public class Transform {

	private int rotation;
	private int horizontalScale;
	private int verticalScale;
	private int alpha;
	
	public Transform() {
		reset();
	}
	
	/**
	 * Resets all of this transform's properties to their original/default values.
	 */
	public void reset() {
		rotation = 0;
		horizontalScale = 100;
		verticalScale = 100;
		alpha = 100;
	}
	
	/**
	 * Returns true if all of this transform's properties are set to their
	 * original/default values.
	 * @deprecated Check the transform's properties separately, e.g. by using
	 *             {@link #isRotated()} or {@link #isScaled()}.
	 */
	@Deprecated
	public boolean isDefaultTransform() {
		return !isRotated() && !isScaled() && alpha == 100;
	}

	public void setRotation(int rotation) {
		if (rotation > 0) {
			this.rotation = rotation % 360;
		} else {
			this.rotation = rotation % -360;
		}
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
	
	public void setScale(int scale) {
		setHorizontalScale(scale);
		setVerticalScale(scale);
	}

	public void setHorizontalScale(int horizontalScale) {
		this.horizontalScale = horizontalScale;
	}
	
	public int getHorizontalScale() {
		return horizontalScale;
	}

	public void setVerticalScale(int verticalScale) {
		this.verticalScale = verticalScale;
	}
	
	public int getVerticalScale() {
		return verticalScale;
	}
	
	public boolean isScaled() {
		return horizontalScale != 100 || verticalScale != 100;
	}

	public void setAlpha(int alpha) {
		this.alpha = MathUtils.clamp(alpha, 0, 100);
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Transform) {
			Transform other = (Transform) o;
			return rotation == other.rotation && 
					horizontalScale == other.horizontalScale &&
					verticalScale == other.verticalScale &&
					alpha == other.alpha;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(rotation, horizontalScale, verticalScale, alpha);
	}
	
	@Override
	public String toString() {
		return String.format("Transform(rotation=%d, scale=%d/%d, alpha=%d)",
				rotation, horizontalScale, verticalScale, alpha);
	}
}
