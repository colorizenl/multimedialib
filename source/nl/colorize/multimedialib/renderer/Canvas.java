//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import lombok.Getter;
import nl.colorize.multimedialib.math.Coordinate;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Size;
import nl.colorize.util.TextUtils;

/**
 * Defines how the application graphics should be displayed. This consists of
 * both the canvas size and the targeted framerate.
 * <p>
 * In most applications, the canvas resolution is independent of the screen
 * resolution of the device. This allows applications to have a reasonably
 * consistent user interface across devices with different screen sizes, as
 * the canvas can be scaled depending on the difference between preferred
 * canvas size and the actual screen size. The canvas is then used to convert
 * between the two coordinate systems. How this scaling behaves when faced
 * with different screen sizes, resolutions, and aspect ratios is controlled
 * by the canvas' {@link ScaleStrategy}.
 * <p>
 * In some cases no scaling is necessary, and the application will simply
 * use the native resolution of the screen. This is referred to as a
 * "flexible" or "native" canvas, which does not have a preferred size or
 * aspect ratio and will simply translate screen coordinates 1-to-1.
 */
@Getter
public class Canvas {

    private Size preferredSize;
    private ScaleStrategy scaleStrategy;
    private Size screenSize;
    private Coordinate screenOffset;

    public Canvas(Size preferredSize, ScaleStrategy scaleStrategy) {
        this.preferredSize = preferredSize;
        this.scaleStrategy = scaleStrategy;
        this.screenSize = preferredSize;
        this.screenOffset = new Coordinate(0, 0);
    }

    public Canvas(int preferredWidth, int preferredHeight, ScaleStrategy scaleStrategy) {
        this(new Size(preferredWidth, preferredHeight), scaleStrategy);
    }

    /**
     * Sets the screen dimensions to the specified values. This method should be
     * called by the renderer when the application window is first created, and
     * whenever the window is resized.
     */
    public void resizeScreen(int screenWidth, int screenHeight) {
        screenSize = new Size(Math.max(screenWidth, 1), Math.max(screenHeight, 1));
    }

    /**
     * Offset the canvas position on the screen. This is mainly needed for when
     * the screen contains system UI such as a status bar or window title where
     * no application graphics can be displayed.
     */
    public void offsetScreen(int offsetX, int offsetY) {
        screenOffset = new Coordinate(offsetX, offsetY);
    }

    public int getWidth() {
        return Math.round(screenSize.width() / getZoomLevel());
    }

    public int getHeight() {
        return Math.round(screenSize.height() / getZoomLevel());
    }

    public Size getSize() {
        return new Size(getWidth(), getHeight());
    }

    public Rect getBounds() {
        return new Rect(0f, 0f, getWidth(), getHeight());
    }

    public Point2D getCenter() {
        return new Point2D(getWidth() / 2f, getHeight() / 2f);
    }

    public boolean isLandscape() {
        return screenSize.width() >= screenSize.height();
    }

    public boolean isPortait() {
        return !isLandscape();
    }

    /**
     * Returns the zoom level that indicates how canvas pixels should be
     * displayed relative to screen pixels. This is based on the current
     * screen size, the canvas' preferred size, and its {@link ScaleStrategy}.
     */
    public float getZoomLevel() {
        return scaleStrategy.getZoomLevel(this);
    }

    public float toCanvasX(int screenX) {
        return (screenX - screenOffset.x()) / getZoomLevel();
    }

    public float toCanvasX(Point2D point) {
        return toCanvasX(Math.round(point.x()));
    }

    public float toCanvasY(int screenY) {
        return (screenY - screenOffset.y()) / getZoomLevel();
    }

    public float toCanvasY(Point2D point) {
        return toCanvasY(Math.round(point.y()));
    }

    public float toScreenX(float canvasX) {
        return canvasX * getZoomLevel() + screenOffset.x();
    }

    public float toScreenX(Point2D point) {
        return toScreenX(point.x());
    }

    public float toScreenY(float canvasY) {
        return canvasY * getZoomLevel() + screenOffset.y();
    }

    public float toScreenY(Point2D point) {
        return toScreenY(point.y());
    }

    @Override
    public String toString() {
        float zoomLevel = getZoomLevel();
        return getWidth() + "x" + getHeight() + " @ " + TextUtils.numberFormat(zoomLevel, 1) + "x";
    }
}
