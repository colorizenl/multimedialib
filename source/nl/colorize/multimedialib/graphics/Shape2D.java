//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;

/**
 * Graphic type that displays a two-dimensional shape in a solid color. Just
 * like sprites, shapes can be transformed (rotated, scaled) before they are
 * displayed. 
 */
public class Shape2D extends StandardGraphic {

	private Rect shape;
	private ColorRGB color;
	
	/**
	 * Creates a {@code Shape2D} instance from the specified shape. Note that the
	 * shape will be drawn <i>around</i> the x and y coordinates.
	 */
	public Shape2D(Rect shape, ColorRGB color) {
		//TODO provide a constructor that accepts any type of shape and change
		//     getShape() to also return that.
		this.shape = shape;
		this.color = color;
	}
	
	/**
	 * Creates a {@code Shape2D} instance from a rectangle with the specified
	 * dimensions.
	 */
	public Shape2D(int width, int height, ColorRGB color) {
		this(new Rect(0, 0, width, height), color);
	}
	
	/**
	 * Returns the shape that is represented by this instance. Note that the
	 * position at which the shape is drawn depends on the current x and y
	 * coordinates, use {@link #getShapeCurrent()} to access that.
	 */
	public Shape getShapeOriginal() {
		return shape;
	}
	
	/**
	 * Returns the shape represented by this instance, taking the current
	 * position and transform into account.
	 */
	public Shape getShapeCurrent() {
		int x = position.getX();
		int y = position.getY();
		int width = shape.getWidth();
		int height = shape.getHeight();
		
		if (transform.isRotated() || transform.isScaled()) {
			width = Math.round(width * 0.01f * transform.getHorizontalScale());
			height = Math.round(height * 0.01f * transform.getVerticalScale());
		
			int[] points = {
				x - width / 2, y - height / 2,
				x + width / 2, y - height / 2,
				x + width / 2, y + height / 2,
				x - width / 2, y + height / 2
			};
			
			Polygon current = new Polygon(points);
			current.rotateDegrees(transform.getRotation(), x, y);
			return current;
		} else {
			return new Rect(x - width / 2, y - height / 2, width, height);
		}
	}
	
	public Shape getBounds() {
		return getShapeCurrent();
	}
	
	public void setColor(ColorRGB color) {
		this.color = color;
	}
	
	public ColorRGB getColor() {
		return color;
	}
}
