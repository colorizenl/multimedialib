//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

/**
 * Defines the resolution of the application graphics independently from
 * the screen resolution of the device. This allows applications to have
 * a reasonably consistent user interface across devices with different
 * screen sizes, as the canvas can be scaled depending on the difference
 * between preferred canvas size and the actual screen size. This class
 * is then used to convert between the two coordinate systems.
 * <p>
 * In some cases no scaling is necessary, and the application will simply
 * use the native resolution of the screen. This is referred to as a
 * "flexible" canvas, which does not have a preferred size or aspect ratio
 * and will simply translate coordinates 1-to-1.
 */
public class Canvas {

    private int preferredWidth;
    private int preferredHeight;
    private ZoomStrategy zoomStrategy;

    private int screenWidth;
    private int screenHeight;
    private int offsetX;
    private int offsetY;

    public enum ZoomStrategy {
        FLEXIBLE,
        ZOOM_IN,
        ZOOM_OUT,
        ZOOM_BALANCED
    }

    /**
     * Creates a new canvas with the specified zoom strategy. Zooming will only
     * occur if the canvas cannot be displayed at its preferred size.
     */
    public Canvas(int preferredWidth, int preferredHeight, ZoomStrategy zoomStrategy) {
        resize(preferredWidth, preferredHeight);
        this.zoomStrategy = zoomStrategy;

        this.screenWidth = preferredWidth;
        this.screenHeight = preferredHeight;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    /**
     * Changes the strategy used by the canvas at runtime.
     */
    public void changeStrategy(ZoomStrategy zoomStrategy) {
        this.zoomStrategy = zoomStrategy;
    }
    
    /**
     * Changes the canvas size to the specified dimensions.
     * <p>
     * This method should be used judiciously. Applications should generally
     * not resize the canvas at runtime, due to the negative impact on user
     * experience. Obviously, the screen or window *can* be resized, but the
     * canvas' zoom strategy is already capable of handling such changes.
     */
    public void resize(int preferredWidth, int preferredHeight) {
        Preconditions.checkArgument(preferredWidth > 0 && preferredHeight > 0,
            "Invalid canvas dimensions: " + preferredWidth + "x" + preferredHeight);

        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
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
        float horizontalZoom = (float) screenWidth / (float) preferredWidth;
        float verticalZoom = (float) screenHeight / (float) preferredHeight;

        switch (zoomStrategy) {
            case FLEXIBLE : return 1f;
            case ZOOM_IN : return Math.max(horizontalZoom, verticalZoom);
            case ZOOM_OUT : return Math.min(horizontalZoom, verticalZoom);
            case ZOOM_BALANCED : return (horizontalZoom + verticalZoom) / 2f;
            default : throw new AssertionError();
        }
    }

    public float toCanvasX(int screenX) {
        return (screenX - offsetX) / getZoomLevel();
    }

    public float toCanvasY(int screenY) {
        return (screenY - offsetY) / getZoomLevel();
    }

    public float toScreenX(float canvasX) {
        return canvasX * getZoomLevel() + offsetX;
    }

    public float toScreenY(float canvasY) {
        return canvasY * getZoomLevel() + offsetY;
    }

    @Override
    public String toString() {
        return getWidth() + "x" + getHeight();
    }

    /**
     * Creates a canvas that resizes itself to match the screen size, always
     * keeping a zoom level of 1.0. The provided width and height are only
     * used to initialize the canvas but will not be used afterwards.
     */
    public static Canvas flexible(int initialWidth, int initialHeight) {
        return new Canvas(initialWidth, initialHeight, ZoomStrategy.FLEXIBLE);
    }

    /**
     * Creates a canvas with fixed dimensions. The canvas will be scaled to
     * match the current screen size. Note that it might not be possible to
     * retain the preferred canvas dimensions in all cases, as the screen
     * might have a different aspect ratio than the canvas.
     *
     * @deprecated This method is not clear enough on how the canvas will
     *             behave at different aspect ratios. Use
     *             {@link #zoomIn(int, int)} or {@link #zoomOut(int, int)}
     *             instead to define an explicit zoom strategy.
     */
    @Deprecated
    public static Canvas fixed(int preferredWidth, int preferredHeight) {
        return zoomOut(preferredWidth, preferredHeight);
    }

    /**
     * Creates a canvas with the specified dimensions, that will scale based
     * on the smallest screen dimension. This means that the canvas will
     * seem zoomed in when the screen and the canvas have very different
     * aspect ratios.
     */
    public static Canvas zoomIn(int preferredWidth, int preferredHeight) {
        return new Canvas(preferredWidth, preferredHeight, ZoomStrategy.ZOOM_IN);
    }

    /**
     * Creates a canvas with the specified dimensions, that will scale based
     * on the largest screen dimension. This means that the canvas will
     * seem zoomed out when the screen and the canvas have very different
     * aspect ratios.
     */
    public static Canvas zoomOut(int preferredWidth, int preferredHeight) {
        return new Canvas(preferredWidth, preferredHeight, ZoomStrategy.ZOOM_OUT);
    }

    /**
     * Creates a canvas with the specified dimensions, that will scale in a
     * way that strikes a balance between the preferred aspect ratio and the
     * actual aspect ratio of the screen.
     */
    public static Canvas zoomBalanced(int preferredWidth, int preferredHeight) {
        return new Canvas(preferredWidth, preferredHeight, ZoomStrategy.ZOOM_BALANCED);
    }
}
