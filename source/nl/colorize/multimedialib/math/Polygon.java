//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A two-dimensional convex polygon with float precision coordinates. The
 * polygon is described by a number of points, with the polygon's outline
 * consisting of lines from each point to the next.
 */
public record Polygon(List<Point2D> points) implements Shape {

    public Polygon(List<Point2D> points) {
        Preconditions.checkArgument(points.size() >= 3,
            "Convex polygon must have at least 3 points, got " + points.size());

        this.points = List.copyOf(points);
    }

    /**
     * Returns an array that contains the X and Y coordinates for all points
     * within this polygon, in the format {@code [x0, y0, x1, y1, ...]}.
     */
    public float[] toPoints() {
        float[] result = new float[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            result[i * 2] = points.get(i).x();
            result[i * 2 + 1] = points.get(i).y();
        }
        return result;
    }

    public int getNumPoints() {
        return points.size();
    }

    public Point2D getPoint(int n) {
        return points.get(n);
    }

    public float getPointX(int n) {
        return points.get(n).x();
    }

    public float getPointY(int n) {
        return points.get(n).y();
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
        float x2 = points.getLast().x();
        float y2 = points.getLast().y();

        for (int i = 0; i < points.size(); x2 = x1, y2 = y1, i++) {
            x1 = points.get(i).x();
            y1 = points.get(i).y();

            if ((y1 < py && y2 >= py) || (y1 >= py && y2 < py)) {
                if ((py - y1) / (y2 - y1) * (x2 - x1) < (px - x1)) {
                    oddNodes = !oddNodes;
                }
            }
        }

        return oddNodes;
    }
    
    private boolean isPointOnLineSegment(float px, float py) {
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D current = points.get(i);
            Point2D next = points.get(i + 1);

            if (isPointOnLineSegment(current.x(), current.y(), next.x(), next.y(), px, py)) {
                return true;
            }
        }

        return isPointOnLineSegment(points.getLast().x(), points.getLast().y(),
            points.getFirst().x(), points.getFirst().y(), px, py);
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
     * Returns true if this polygon is located either partially or entirely
     * within the specified other polygon. This requires both polygons to
     * be convex. The check itself is based on the Separating Axis Theorem.
     */
    public boolean intersects(Polygon other) {
        return !overlaps(points, other.points) && !overlaps(other.points, points);
    }

    private boolean overlaps(List<Point2D> a, List<Point2D> b) {
        for (int i = 0; i < a.size(); i++) {
            Point2D current = a.get(i);
            Point2D next = a.get((i + 1) % a.size());

            Point2D edge = new Point2D(next.x() - current.x(), next.y() - current.y());
            Point2D axis = new Point2D(-edge.y(), edge.x());

            float[] projectionA = project(a, axis);
            float[] projectionB = project(b, axis);

            if (projectionA[1] < projectionB[0] || projectionB[1] < projectionA[0]) {
                return true;
            }
        }

        return false;
    }

    private float[] project(List<Point2D> points, Point2D axis) {
        float min = dot(axis, points.getFirst());
        float max = min;

        for (int i = 1; i < points.size(); i++) {
            float p = dot(axis, points.get(i));
            min = (float) Math.min(p, min);
            max = (float) Math.max(p, max);
        }

        return new float[] {min, max};
    }

    private float dot(Point2D p1, Point2D p2) {
        return p1.x() * p2.x() + p1.y() * p2.y();
    }

    /**
     * Returns the smallest possible axis-aligned rectangle that contains this
     * polygon.
     */
    @Override
    public Rect getBoundingBox() {
        float minX = points.getFirst().x();
        float minY = points.getFirst().y();
        float maxX = minX;
        float maxY = minY;

        for (int i = 1; i < points.size(); i++) {
            minX = Math.min(minX, points.get(i).x());
            minY = Math.min(minY, points.get(i).y());
            maxX = Math.max(maxX, points.get(i).x());
            maxY = Math.max(maxY, points.get(i).y());
        }

        return new Rect(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public Point2D getCenter() {
        return getBoundingBox().getCenter();
    }

    /**
     * Subdivides this polygon into a number of triangles. This requires this
     * polygon to be convex.
     */
    public List<Polygon> subdivide() {
        if (points.size() == 3) {
            return List.of(this);
        }

        List<Point2D> vertices = subdivideVertices();
        List<Polygon> triangles = new ArrayList<>();

        for (int i = 0; i < vertices.size(); i += 3) {
            triangles.add(new Polygon(List.of(
                vertices.get(i),
                vertices.get(i + 1),
                vertices.get(i + 2)
            )));
        }

        return triangles;
    }

    private List<Point2D> subdivideVertices() {
        float[] points = toPoints();
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

    /**
     * Applies the specified mapping function to each of the points within
     * this polygon, and returns a new polygon based on those points.
     */
    public Polygon map(Function<Point2D, Point2D> mapper) {
        List<Point2D> newPoints = points.stream()
            .map(mapper)
            .toList();

        return new Polygon(newPoints);
    }

    /**
     * Returns a new {@link Polygon} that is based on adding the specified
     * delta to every point in this polygon.
     */
    public Polygon move(Point2D delta) {
        return map(p -> p.add(delta));
    }

    /**
     * Returns a new {@link Polygon} that is based on rotating this polygon
     * around {@link Point2D#ORIGIN}.
     */
    public Polygon rotate(Angle angle) {
        return map(p -> {
            float radians = angle.getRadians();
            double rotatedX = p.x() * Math.cos(radians) - p.y() * Math.sin(radians);
            double rotatedY = p.x() * Math.sin(radians) + p.y() * Math.cos(radians);
            return new Point2D((float) rotatedX, (float) rotatedY);
        });
    }

    @Override
    public Polygon reposition(Point2D offset) {
        return map(p -> p.add(offset));
    }

    @Override
    public String toString() {
        return "Polygon";
    }

    /**
     * Factory method that creates a polygon from an array of points, in the
     * format {@code [x0, y0, x1, y1, ...]}.
     */
    public static Polygon fromPoints(float... points) {
        List<Point2D> result = new ArrayList<>();
        for (int i = 0; i < points.length; i += 2) {
            result.add(new Point2D(points[i], points[i + 1]));
        }
        return new Polygon(result);
    }

    /**
     * Convenience method to create a rectangle-shaped polygon with its center
     * located at the specified origin.
     */
    public static Polygon createRectangle(Point2D origin, float width, float height) {
        Preconditions.checkArgument(width > 0, "Invalid width: " + width);
        Preconditions.checkArgument(height > 0, "Invalid height: " + height);

        return new Polygon(List.of(
            new Point2D(origin.x() - width / 2, origin.y() - height / 2),
            new Point2D(origin.x() + width / 2, origin.y() - height / 2),
            new Point2D(origin.x() + width / 2, origin.y() + height / 2),
            new Point2D(origin.x() - width / 2, origin.y() + height / 2)
        ));
    }

    /**
     * Convenience method to create a polygon in the shape of a circle. The
     * circle will be centered around the specified origin.
     */
    public static Polygon createCircle(Point2D origin, float radius, int numPoints) {
        Preconditions.checkArgument(numPoints >= 4, "Too few points: " + numPoints);
        Preconditions.checkArgument(radius > EPSILON, "Invalid radius: " + radius);

        List<Point2D> points = new ArrayList<>();

        for (int i = 0; i < numPoints; i++) {
            Vector vector = new Vector(i * (360f / numPoints), radius);
            points.add(new Point2D(origin.x() + vector.getX(), origin.y() + vector.getY()));
        }

        return new Polygon(points);
    }

    /**
     * Convenience method to create a polygon in the shape of a circle. The
     * circle will be centered around {@link Point2D#ORIGIN}.
     */
    public static Polygon createCircle(float radius, int numPoints) {
        return createCircle(Point2D.ORIGIN, radius, numPoints);
    }

    /**
     * Convenience method to create a polygon in the shape of an ecllipse. The
     * ellipse will be centered around the specified origin.
     */
    public static Polygon createEllipse(Point2D origin, float radiusX, float radiusY, int numPoints) {
        Preconditions.checkArgument(numPoints >= 4, "Too few points: " + numPoints);
        Preconditions.checkArgument(radiusX > EPSILON, "Invalid X-radius: " + radiusX);
        Preconditions.checkArgument(radiusY > EPSILON, "Invalid Y-radius: " + radiusY);

        List<Point2D> points = new ArrayList<>();
        float aspectRatio = radiusY / radiusX;

        for (int i = 0; i < numPoints; i++) {
            Vector vector = new Vector(i * (360f / numPoints), radiusX);
            float x = origin.x() + vector.getX();
            float y = origin.y() + (vector.getY() * aspectRatio);
            points.add(new Point2D(x, y));
        }

        return new Polygon(points);
    }

    /**
     * Convenience method to create a polygon in the shape of an ecllipse. The
     * ellipse will be centered around {@link Point2D#ORIGIN}.
     */
    public static Polygon createEllipse(float radiusX, float radiusY, int numPoints) {
        return createEllipse(Point2D.ORIGIN, radiusX, radiusY, numPoints);
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

        return new Polygon(List.of(
            origin,
            new Point2D(origin.x() + left.getX(), origin.y() + left.getY()),
            new Point2D(origin.x() + center.getX(), origin.y() + center.getY()),
            new Point2D(origin.x() + right.getX(), origin.y() + right.getY())
        ));
    }
}
