//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Point;

/**
 * Mutable graphic that has a position, z-index, and optional transformation.
 */
public abstract class StandardGraphic implements Graphic {
	
	protected Point position;
	protected int zIndex;
	protected Transform transform;

	public StandardGraphic() {
		position = new Point(0, 0);
		zIndex = 0;
		transform = new Transform();
	}
	
	public void setPosition(Point p) {
		position.set(p.getX(), p.getY());
	}
	
	public void setPosition(int x, int y) {
		position.set(x, y);
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setX(int x) {
		position.setX(x);
	}
	
	public int getX() {
		return position.getX();
	}
	
	public void setY(int y) {
		position.setY(y);
	}
	
	public int getY() {
		return position.getY();
	}
	
	public void move(int deltaX, int deltaY) {
		position.set(position.getX() + deltaX, position.getY() + deltaY);
	}
	
	public void setZIndex(int zIndex) {
		this.zIndex = zIndex;
	}
	
	public int getZIndex() {
		return zIndex;
	}
	
	public final Transform getTransform() {
		return transform;
	}
}
