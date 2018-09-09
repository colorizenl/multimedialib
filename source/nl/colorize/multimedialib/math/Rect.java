//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

/**
 * A two-dimensional axis-aligned rectangle with integer precision coordinates. 
 */
public class Rect implements Shape {
    
    private int x;
    private int y;
    private int width;
    private int height;
    
    public Rect(int x, int y, int width, int height) {
        set(x, y, width, height);
    }
    
    public void set(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }
    
    public int getEndX() {
        return x + width;
    }
    
    public int getEndY() {
        return y + height;
    }
    
    public int getCenterX() {
        return x + width / 2;
    }
    
    public int getCenterY() {
        return y + height / 2;
    }
    
    public boolean contains(Point2D p) {
        return contains((int) p.getX(), (int) p.getY());
    }
    
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    public boolean contains(Shape s) {
        if (s instanceof Rect) {
            Rect r = (Rect) s;
            return contains(r.x, r.y, r.width, r.height);
        } else {
            return s.toPolygon().intersects(this);
        }
    }
    
    public boolean contains(int rx, int ry, int rwidth, int rheight) {
        return rx >= x && rx + rwidth <= x + width && ry >= y && ry + rheight <= y + height;
    }
    
    public boolean intersects(Shape s) {
        if (s instanceof Rect) {
            Rect r = (Rect) s;
            return intersects(r.x, r.y, r.width, r.height);
        } else {
            return s.toPolygon().intersects(this);
        }
    }
    
    public boolean intersects(int rx, int ry, int rwidth, int rheight) {
        return !(rx + rwidth < x || rx > x + width || ry + rheight < y || ry > y + height);
    }
    
    public Polygon toPolygon() {
        int[] points = {x, y, x + width, y, x + width, y + height, x, y + height};
        return new Polygon(points);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Rect) {
            Rect r = (Rect) o;
            return x == r.x && y == r.y && width == r.width && height == r.height;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return x * 1_000_000 + y * 10_000 + width * 100 + height;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d, %d, %d]", x, y, width, height);
    }
}
