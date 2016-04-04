//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * A two-dimensional coordinate with integer precision coordinates.
 */
public class Point {

	private int x;
	private int y;
	
	public Point(int x, int y) {
		set(x, y);
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getX() {
		return x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getY() {
		return y;
	}
	
	/**
	 * Returns the distance between this point and point {@code p}.
	 */
	public int distanceTo(Point p) {
		return distanceTo(p.x, p.y);
	}
	
	/**
	 * Returns the distance between this point and the point {@code (px, py)}.
	 */
	public int distanceTo(int px, int py) {
		int deltaX = Math.abs(px - x);
		int deltaY = Math.abs(py - y);
		return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point p = (Point) o;
			return x == p.x && y == p.y;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return x * 10000 + y;
	}
	
	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
}
