//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.Arrays;

/**
 * Describes a two-dimensional convex polygon with integer precision coordinates.
 * The polygon is described by an array of points, e.g. [x0, y0, x1, y1, ...].
 */
public class Polygon implements Shape {

	private int[] points;
	
	public Polygon(int[] points) {
		setPoints(points);
	}

	private void checkPoints(int[] points) {
		if (points.length < 3) {
			throw new IllegalArgumentException("Convex polygon must have at least 3 points");
		}
		
		if (points.length % 2 == 1) {
			throw new IllegalArgumentException("Invalid points: " + Arrays.toString(points));
		}
	}
	
	public void setPoints(int[] points) {
		checkPoints(points);
		this.points = points;
	}

	public int[] getPoints() {
		return points;
	}
	
	public int getNumPoints() {
		return points.length / 2;
	}
	
	public int getPointX(int n) {
		return points[2 * n];
	}
	
	public int getPointY(int n) {
		return points[2 * n + 1];
	}

	public void move(int dx, int dy) {
		for (int i = 0; i < points.length; i += 2) {
			points[i] += dx;
			points[i + 1] += dy;
		}
	}
	
	public void rotateDegrees(int degrees, int ox, int oy) {
		if (degrees != 0) {
			rotateRadians(Math.toRadians(degrees), ox, oy);
		}
	}
	
	public void rotateRadians(double radians, int ox, int oy) {
		for (int i = 0; i < points.length; i += 2) {
			int x0 = points[i];
			int y0 = points[i + 1];
			double x = Math.cos(radians) * (x0 - ox) - Math.sin(radians) * (y0 - oy) + ox;
			double y = Math.sin(radians) * (x0 - ox) + Math.cos(radians) * (y0 - oy) + oy;
			points[i] = (int) Math.round(x);
			points[i + 1] = (int) Math.round(y);
		}
	}
	
	public boolean contains(Point p) {
		return contains(p.getX(), p.getY());
	}
	
	public boolean contains(int px, int py) {
		return isPointInPolygon(px, py) || isPointOnLineSegment(px, py);
	}
	
	private boolean isPointInPolygon(int px, int py) {
		// Implementation based on the suggestions from
		// http://www.java-gaming.org/index.php?topic=26013.0 
		boolean oddNodes = false;
		float x1 = 0f;
		float y1 = 0f;
		float x2 = points[points.length - 2];
		float y2 = points[points.length - 1];
		for (int i = 0; i < points.length; x2 = x1, y2 = y1, i += 2) {
			x1 = points[i];
			y1 = points[i + 1];
			if (((y1 < py) && (y2 >= py)) || (y1 >= py) && (y2 < py)) {
				if ((py - y1) / (y2 - y1) * (x2 - x1) < (px - x1)) {
					oddNodes = !oddNodes;
				}
			}
		}
		return oddNodes;
	}
	
	private boolean isPointOnLineSegment(int px, int py) {
		for (int i = 0; i < points.length - 2; i += 2) {
			if (isPointOnLineSegment(points[i], points[i + 1], points[i + 2], points[i + 3], px, py)) {
				return true;
			}
		}
		return isPointOnLineSegment(points[points.length - 2], points[points.length - 1], 
				points[0], points[1], px, py);
	}
	
	private boolean isPointOnLineSegment(int x0, int y0, int x1, int y1, int px, int py) {
		int crossproduct = (py - y0) * (x1 - x0) - (px - x0) * (y1 - y0);
		if (crossproduct != 0) {
			return false;
		}
		int dotproduct = (px - x0) * (x1 - x0) + (py - y0) * (x1 - y0);
		if (dotproduct < 0) {
			return false;
		}
		int squaredLength = (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0);
		return dotproduct <= squaredLength;
	}

	public boolean contains(Shape s) {
		int[] pPoints = s.toPolygon().getPoints();
		for (int i = 0; i < pPoints.length; i += 2) {
			if (!contains(pPoints[i], pPoints[i + 1])) {
				return false;
			}
		}
		return true;
	}
	
	public boolean intersects(Shape s) {
		Polygon p = s.toPolygon();
		return contains(p) || p.contains(this) || checkPolygonIntersects(p); 
	}
	
	private boolean checkPolygonIntersects(Polygon p) {
		// Implementation based on the polygon/polygon collision check
		// from Slick (http://slick.cokeandcode.com).
		int[] pPoints = p.getPoints();
		
        double unknownA;
        double unknownB;
        
		for (int i = 0; i < points.length; i += 2) {
			int iNext = i + 2;
			if (iNext >= points.length)
				iNext = 0;
                
			for (int j = 0; j < pPoints.length; j += 2) {
				int jNext = j + 2;
				if (jNext >= pPoints.length)
					jNext = 0;

				unknownA = (((points[iNext] - points[i]) * (double) (pPoints[j + 1] - points[i + 1])) - 
						((points[iNext + 1] - points[i + 1]) * (pPoints[j] - points[i]))) / 
						(((points[iNext + 1] - points[i + 1]) * (pPoints[jNext] - pPoints[j])) - 
						((points[iNext] - points[i]) * (pPoints[jNext + 1] - pPoints[j + 1])));
				
				unknownB = (((pPoints[jNext] - pPoints[j]) * (double) (pPoints[j + 1] - points[i + 1])) - 
						((pPoints[jNext + 1] - pPoints[j + 1]) * (pPoints[j] - points[i]))) / 
						(((points[iNext + 1] - points[i + 1]) * (pPoints[jNext] - pPoints[j])) - 
						((points[iNext] - points[i]) * (pPoints[jNext + 1] - pPoints[j + 1])));
                
				if (unknownA >= 0 && unknownA <= 1 && unknownB >= 0 && unknownB <= 1) {
					return true;
				}
            }
        }

        return false;
	}
	
	public Polygon toPolygon() {
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Polygon) {
			Polygon other = (Polygon) o;
			return Arrays.equals(points, other.points);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(points);
	}

	@Override
	public String toString() {
		return Arrays.toString(points);
	}
}
