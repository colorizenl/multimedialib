//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Root interface of all objects that represent some kind of geometric shape. 
 */
public interface Shape {
	
	/**
	 * Returns true if the specified point {@code p} is located within this shape.
	 * This includes cases where the point is located on one of the edges. 
	 */
	public boolean contains(Point p);

	/**
	 * Returns true if the specified point is located within this shape.
	 * This includes cases where the point is located on one of the edges.
	 */
	public boolean contains(int px, int py);
	
	/**
	 * Returns true if the shape {@code s} is entirely located within this shape.
	 * This includes cases where the shape is located on this shape's edges.
	 */
	public boolean contains(Shape s);
	
	/**
	 * Returns true if the shape {@code s} is entirely or partially located within
	 * this shape.
	 */
	public boolean intersects(Shape s);
	
	public Polygon toPolygon();
}
