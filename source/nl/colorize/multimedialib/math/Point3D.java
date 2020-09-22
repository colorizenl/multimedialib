//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * Point with X, Y, and Z coordinates with float precision.
 */
public class Point3D {

    private float x;
    private float y;
    private float z;

    public Point3D(float x, float y, float z) {
        set(x, y, z);
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Point3D p) {
        set(p.getX(), p.getY(), p.getZ());
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getX() {
        return x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getZ() {
        return z;
    }

    public void add(float deltaX, float deltaY, float deltaZ) {
        x += deltaX;
        y += deltaY;
        z += deltaZ;
    }

    public Point3D normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        return new Point3D(x / length, y / length, z / length);
    }

    public Point3D copy() {
        return new Point3D(x, y, z);
    }

    @Override
    public String toString() {
        return "(" + Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ")";
    }
}
