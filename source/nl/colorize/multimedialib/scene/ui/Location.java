//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;

/**
 * A point that does not use absolute coordinates, but is instead relative to
 * the canvas borders. This is useful for user interface widgets, that tend to
 * be positioned relative rather than absolute.
 */
public class Location {

    private Canvas canvas;
    private BorderAlign xAlign;
    private float xOffset;
    private BorderAlign yAlign;
    private float yOffset;

    private enum BorderAlign {
        LEFT,
        RIGHT,
        CENTER,
        TOP,
        BOTTOM
    }

    private Location(Canvas canvas, BorderAlign xAlign, float xOffset, BorderAlign yAlign, float yOffset) {
        Preconditions.checkArgument(xOffset >= 0f && yOffset >= 0f,
            "Negative coordinates are not supported: " + xOffset + ", " + yOffset);

        this.canvas = canvas;
        this.xAlign = xAlign;
        this.xOffset = xOffset;
        this.yAlign = yAlign;
        this.yOffset = yOffset;
    }

    public float getX() {
        switch (xAlign) {
            case LEFT : return xOffset;
            case RIGHT : return canvas.getWidth() - xOffset;
            case CENTER : return canvas.getWidth() / 2f;
            default : throw new IllegalArgumentException("Invalid alignment: " + xAlign);
        }
    }

    public float getY() {
        switch (yAlign) {
            case TOP : return yOffset;
            case BOTTOM : return canvas.getHeight() - yOffset;
            case CENTER : return canvas.getHeight() / 2f;
            default : throw new IllegalArgumentException("Invalid alignment: " + yAlign);
        }
    }

    public Point2D toPoint() {
        return new Point2D(getX(), getY());
    }

    /**
     * Creates a new location that is positioned at an offset relative to this
     * location.
     */
    public Location relativeTo(float deltaX, float deltaY) {
        float newX = xOffset + (xAlign == BorderAlign.RIGHT ? -deltaX : deltaX);
        float newY = yOffset + (yAlign == BorderAlign.BOTTOM ? -deltaY : deltaY);

        return new Location(canvas, xAlign, newX, yAlign, newY);
    }

    @Override
    public String toString() {
        return xAlign + " " + Math.round(xOffset) + ", " + yAlign + " " + Math.round(yOffset);
    }

    /**
     * Creates a location at fixed coordinates that will not move when the
     * canvas is resized.
     */
    public static Location fixed(float x, float y) {
        return new Location(null, BorderAlign.LEFT, x, BorderAlign.TOP, y);
    }

    public static Location left(Canvas canvas, float x, float y) {
        return new Location(canvas, BorderAlign.LEFT, x, BorderAlign.TOP, y);
    }

    public static Location right(Canvas canvas, float x, float y) {
        return new Location(canvas, BorderAlign.RIGHT, x, BorderAlign.TOP, y);
    }

    public static Location center(Canvas canvas, float y) {
        return new Location(canvas, BorderAlign.CENTER, 0f, BorderAlign.TOP, y);
    }

    public static Location top(Canvas canvas, float x, float y) {
        return new Location(canvas, BorderAlign.LEFT, x, BorderAlign.TOP, y);
    }

    public static Location bottom(Canvas canvas, float x, float y) {
        return new Location(canvas, BorderAlign.LEFT, x, BorderAlign.BOTTOM, y);
    }

    public static Location bottomCenter(Canvas canvas, float y) {
        return new Location(canvas, BorderAlign.CENTER, 0f, BorderAlign.BOTTOM, y);
    }

    public static Location bottomRight(Canvas canvas, float x, float y) {
        return new Location(canvas, BorderAlign.RIGHT, x, BorderAlign.BOTTOM, y);
    }
}
