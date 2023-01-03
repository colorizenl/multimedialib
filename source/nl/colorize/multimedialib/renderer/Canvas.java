//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

/**
 * Defines how the application graphics should be displayed. This consists of
 * both the canvas size and the targeted framerate.
 * <p>
 * In most applications, the canvas resolution is independent of the screen
 * resolution of the device. This allows applications to have a reasonably
 * consistent user interface across devices with different screen sizes, as
 * the canvas can be scaled depending on the difference between preferred
 * canvas size and the actual screen size. This class is then used to convert
 * between the two coordinate systems.
 * <p>
 * In some cases no scaling is necessary, and the application will simply
 * use the native resolution of the screen. This is referred to as a
 * "flexible" canvas, which does not have a preferred size or aspect ratio
 * and will simply translate coordinates 1-to-1.
 */
public class Canvas {

    private int preferredWidth;
    private int preferredHeight;
    private boolean zoom;

    private int screenWidth;
    private int screenHeight;
    private int offsetX;
    private int offsetY;

    private Canvas(int preferredWidth, int preferredHeight, boolean zoom) {
        resize(preferredWidth, preferredHeight);
        this.zoom = zoom;

        this.screenWidth = preferredWidth;
        this.screenHeight = preferredHeight;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    /**
     * Sets the screen dimensions to the specified values. This method should be
     * called by the renderer when the application window is first created, and
     * whenever the window is resized.
     */
    public void resizeScreen(int screenWidth, int screenHeight) {
        Preconditions.checkArgument(screenWidth > 0 && screenHeight > 0,
            "Invalid screen dimensions: " + screenWidth + "x" + screenHeight);

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Offset the canvas position on the screen. This is mainly needed for when
     * the screen contains system UI such as a status bar or window title where
     * no application graphics can be displayed.
     */
    public void offsetScreen(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public int getWidth() {
        return Math.round(screenWidth / getZoomLevel());
    }

    public int getHeight() {
        return Math.round(screenHeight / getZoomLevel());
    }

    public Rect getBounds() {
        return new Rect(0f, 0f, getWidth(), getHeight());
    }

    public Point2D getCenter() {
        return new Point2D(getWidth() / 2f, getHeight() / 2f);
    }

    public float getZoomLevel() {
        if (!zoom) {
            return 1f;
        }

        float horizontalZoom = (float) screenWidth / (float) preferredWidth;
        float verticalZoom = (float) screenHeight / (float) preferredHeight;
        return Math.min(horizontalZoom, verticalZoom);
    }

    public float toCanvasX(int screenX) {
        return (screenX - offsetX) / getZoomLevel();
    }

    public float toCanvasX(Point2D point) {
        return toCanvasX(Math.round(point.getX()));
    }

    public float toCanvasY(int screenY) {
        return (screenY - offsetY) / getZoomLevel();
    }

    public float toCanvasY(Point2D point) {
        return toCanvasY(Math.round(point.getY()));
    }

    public float toScreenX(float canvasX) {
        return canvasX * getZoomLevel() + offsetX;
    }

    public float toScreenX(Point2D point) {
        return toScreenX(point.getX());
    }

    public float toScreenY(float canvasY) {
        return canvasY * getZoomLevel() + offsetY;
    }

    public float toScreenY(Point2D point) {
        return toScreenY(point.getY());
    }

    /**
     * Changes the strategy used by the canvas at runtime. This method is
     * deprecated as application behavior is often unable to handle such a
     * scenario.
     */
    @Deprecated
    public void changeStrategy(boolean zoom) {
        this.zoom = zoom;
    }

    /**
     * Changes the canvas size to the specified dimensions.
     * <p>
     * This method should be used judiciously. Applications should generally
     * not resize the canvas at runtime, due to the negative impact on user
     * experience. Obviously, the screen or window *can* be resized, but the
     * canvas' zoom strategy is already capable of handling such changes.
     */
    @Deprecated
    public void resize(int preferredWidth, int preferredHeight) {
        Preconditions.checkArgument(preferredWidth > 0 && preferredHeight > 0,
            "Invalid canvas dimensions: " + preferredWidth + "x" + preferredHeight);

        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
    }

    @Override
    public String toString() {
        float zoomLevel = getZoomLevel();
        return getWidth() + "x" + getHeight() + " @ " + MathUtils.format(zoomLevel, 1) + "x";
    }

    /**
     * Creates a new canvas that will try to match the preferred size, scaling
     * the canvas if necessary.
     */
    public static Canvas forSize(int preferredWidth, int preferredHeight) {
        return new Canvas(preferredWidth, preferredHeight, true);
    }

    /**
     * Creates a new canvas that will not perform any scaling, and will simply
     * display graphics at the screen's native resolution. A preferred width
     * and height still need to be provided for sitations where the platform
     * allows or requires the application to define its preferred size.
     */
    public static Canvas forNative(int preferredWidth, int preferredHeight) {
        return new Canvas(preferredWidth, preferredHeight, false);
    }
}
