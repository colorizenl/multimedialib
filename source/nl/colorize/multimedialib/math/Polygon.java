//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A two-dimensional convex polygon with float precision coordinates. The
 * polygon is described by an array of points, e.g. [x0, y0, x1, y1, ...].
 */
public record Polygon(float... points) implements Shape {

    public Polygon {
        Preconditions.checkArgument(points.length >= 6,
            "Convex polygon must have at least 3 points, got " + points.length);

        Preconditions.checkArgument(points.length % 2 != 1,
            "Points array must have equal number of X and Y coordinates: " + points.length);
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

    public Point2D getPoint(int n) {
        return new Point2D(getPointX(n), getPointY(n));
    }

    @Override
    public boolean contains(Point2D p) {
        return isPointInPolygon(p.x(), p.y()) || isPointOnLineSegment(p.x(), p.y());
    }

    /**
     * Returns true if the specified point is located within this polygon.
     * Implementation based on http://www.java-gaming.org/index.php?topic=26013.0
     */
    private boolean isPointInPolygon(float px, float py) {
        boolean oddNodes = false;
        float x1 = 0f;
        float y1 = 0f;
        float x2 = points[points.length - 2];
        float y2 = points[points.length - 1];

        for (int i = 0; i < points.length; x2 = x1, y2 = y1, i += 2) {
            x1 = points[i];
            y1 = points[i + 1];

            if ((y1 < py && y2 >= py) || y1 >= py && y2 < py) {
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

    /**
     * Returns true if this polygon intersects with the specified other polygon.
     * Implementation based on <a href="http://slick.cokeandcode.com">Slick</a>.
     */
    public boolean intersects(Polygon p) {
        float[] pPoints = p.points();
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

    /**
     * Returns the smallest possible axis-aligned rectangle that contains this
     * polygon.
     */
    @Override
    public Rect getBoundingBox() {
        float minX = points[0];
        float minY = points[1];
        float maxX = points[0];
        float maxY = points[1];

        for (int i = 2; i < points.length; i += 2) {
            minX = Math.min(minX, points[i]);
            minY = Math.min(minY, points[i + 1]);
            maxX = Math.max(maxX, points[i]);
            maxY = Math.max(maxY, points[i + 1]);
        }

        return new Rect(minX, minY, maxX - minX, maxY - minY);
    }

    public Point2D getCenter() {
        return getBoundingBox().getCenter();
    }

    /**
     * Subdivides this polygon into a number of triangles. This requires this
     * polygon to be convex.
     */
    public List<Polygon> subdivide() {
        if (points.length == 6) {
            return Collections.singletonList(this);
        }

        List<Point2D> vertices = subdivideVertices();
        List<Polygon> triangles = new ArrayList<>();

        for (int i = 0; i < vertices.size(); i += 3) {
            triangles.add(new Polygon(
                vertices.get(i).x(), vertices.get(i).y(),
                vertices.get(i + 1).x(), vertices.get(i + 1).y(),
                vertices.get(i + 2).x(), vertices.get(i + 2).y()
            ));
        }

        return triangles;
    }

    private List<Point2D> subdivideVertices() {
        List<Point2D> vertices = new ArrayList<>();
        Point2D center = getCenter();

        for (int i = 0; i < points.length; i += 2) {
            vertices.add(new Point2D(points[i], points[i + 1]));

            if (i >= 2) {
                vertices.add(center);
                vertices.add(new Point2D(points[i], points[i + 1]));
            }

            if (i == points.length - 2) {
                vertices.add(new Point2D(points[0], points[1]));
                vertices.add(center);
            }
        }

        return vertices;
    }

    @Override
    public Polygon reposition(Point2D offset) {
        float[] newPoints = new float[points.length];

        for (int i = 0; i < points.length; i += 2) {
            newPoints[i] = points[i] + offset.x();
            newPoints[i + 1] = points[i + 1] + offset.y();
        }

        return new Polygon(newPoints);
    }

    @Override
    public String toString() {
        return "Polygon";
    }

    /**
     * Convenience method to create a polygon in the shape of a circle with the
     * specified properties.
     */
    public static Polygon createCircle(Point2D origin, float radius, int numPoints) {
        Preconditions.checkArgument(numPoints >= 4, "Too few points: " + numPoints);
        Preconditions.checkArgument(radius > EPSILON, "Invalid radius: " + radius);

        float[] points = new float[numPoints * 2];

        for (int i = 0; i < numPoints; i++) {
            Vector vector = new Vector(i * (360f / numPoints), radius);
            points[i * 2] = origin.x() + vector.getX();
            points[i * 2 + 1] = origin.y() + vector.getY();
        }

        return new Polygon(points);
    }

    /**
     * Convenience method to create a cone-shaped polygon. The cone's angle
     * indicates in which direction the cone is pointed, its arc indicates
     * its size (in degrees).
     */
    public static Polygon createCone(Point2D origin, Angle angle, float arc, float length) {
        return createCone(origin, angle.degrees(), arc, length);
    }

    /**
     * Convenience method to create a cone-shaped polygon. The cone's angle
     * indicates in which direction the cone is pointed, its arc indicates
     * its size (in degrees).
     */
    public static Polygon createCone(Point2D origin, float angle, float arc, float length) {
        Preconditions.checkArgument(arc > 0f && arc <= 180f, "Invalid arc: " + arc);
        Preconditions.checkArgument(length > 0f, "Invalid length: " + length);

        Vector left = new Vector((angle % 360) - arc / 2f, length);
        Vector center = new Vector((angle % 360), length);
        Vector right = new Vector((angle % 360) + arc / 2f, length);

        return new Polygon(
            origin.x(), origin.y(),
            origin.x() + left.getX(), origin.y() + left.getY(),
            origin.x() + center.getX(), origin.y() + center.getY(),
            origin.x() + right.getX(), origin.y() + right.getY()
        );
    }
}
