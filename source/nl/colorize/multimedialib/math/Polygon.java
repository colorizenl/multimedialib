//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * Describes a two-dimensional convex polygon with float precision coordinates.
 * The polygon is described by an array of points, e.g. [x0, y0, x1, y1, ...].
 */
public class Polygon implements Shape {

    private float[] points;

    public Polygon(float[] points) {
        setPoints(points);
    }

    public void setPoints(float[] points) {
        Preconditions.checkArgument(points.length >= 6, "Convex polygon must have at least 3 points");
        Preconditions.checkArgument(points.length % 2 != 1,
            "Points array must have equal number of X and Y coordinates: " + Arrays.toString(points));

        this.points = points;
    }

    public float[] getPoints() {
        return points;
    }
    
    public int getNumPoints() {
        return points.length / 2;
    }
    
    public float getPointX(int n) {
        return points[2 * n];
    }
    
    public float getPointY(int n) {
        return points[2 * n + 1];
    }

    public void move(float dx, float dy) {
        for (int i = 0; i < points.length; i += 2) {
            points[i] += dx;
            points[i + 1] += dy;
        }
    }

    @Override
    public boolean contains(Point p) {
        return isPointInPolygon(p.getX(), p.getY()) || isPointOnLineSegment(p.getX(), p.getY());
    }

    private boolean isPointInPolygon(float px, float py) {
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
    
    private boolean isPointOnLineSegment(float px, float py) {
        for (int i = 0; i < points.length - 2; i += 2) {
            if (isPointOnLineSegment(points[i], points[i + 1], points[i + 2], points[i + 3], px, py)) {
                return true;
            }
        }

        return isPointOnLineSegment(points[points.length - 2], points[points.length - 1],
            points[0], points[1], px, py);
    }
    
    private boolean isPointOnLineSegment(float x0, float y0, float x1, float y1, float px, float py) {
        float crossproduct = (py - y0) * (x1 - x0) - (px - x0) * (y1 - y0);

        if (crossproduct != 0) {
            return false;
        }

        float dotproduct = (px - x0) * (x1 - x0) + (py - y0) * (x1 - y0);

        if (dotproduct < 0) {
            return false;
        }

        float squaredLength = (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0);
        return dotproduct <= squaredLength;
    }

    public boolean intersects(Polygon p) {
        // Implementation based on the polygon/polygon collision check
        // from Slick (http://slick.cokeandcode.com).
        float[] pPoints = p.getPoints();
        
        double unknownA;
        double unknownB;
        
        for (int i = 0; i < points.length; i += 2) {
            int iNext = i + 2;
            if (iNext >= points.length) {
                iNext = 0;
            }
                
            for (int j = 0; j < pPoints.length; j += 2) {
                int jNext = j + 2;
                if (jNext >= pPoints.length) {
                    jNext = 0;
                }

                unknownA = (((points[iNext] - points[i]) * (pPoints[j + 1] - points[i + 1])) -
                        ((points[iNext + 1] - points[i + 1]) * (pPoints[j] - points[i]))) / 
                        (((points[iNext + 1] - points[i + 1]) * (pPoints[jNext] - pPoints[j])) - 
                        ((points[iNext] - points[i]) * (pPoints[jNext + 1] - pPoints[j + 1])));
                
                unknownB = (((pPoints[jNext] - pPoints[j]) * (pPoints[j + 1] - points[i + 1])) -
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Polygon) {
            Polygon other = (Polygon) o;

            if (points.length != other.points.length) {
                return false;
            }

            for (int i = 0; i < points.length; i++) {
                if (Math.abs(points[i] - other.points[i]) > EPSILON) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(points);
    }

    @Override
    public String toString() {
        return Arrays.toString(points);
    }

    public static Polygon createCircle(float x, float y, float radius, int numPoints) {
        Preconditions.checkArgument(numPoints >= 4,
            "Circle polygon must consist of at least 4 points, got " + numPoints);
        Preconditions.checkArgument(radius > EPSILON, "Invalid radius: " + radius);

        float[] points = new float[numPoints * 2];
        Vector vector = new Vector(0f, radius);

        for (int i = 0; i < numPoints; i++) {
            vector.setDirection(i * (360f / numPoints));

            points[i * 2] = x + vector.getX();
            points[i * 2 + 1] = y + vector.getY();
        }

        return new Polygon(points);
    }
}
