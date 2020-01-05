//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.Formatting;

/**
 * Represents the resolution of the application graphics independently from
 * the device's screen resolution. The canvas is initially created with a
 * preferred width and height. When the screen or window is resized, the
 * canvas will also be resized while keeping its original aspect ratio.
 * <p>
 * Having a consistent canvas size across different devices ensures a
 * consistent user experience in situations where the same application is
 * used across a variety of devices with different screen sizes. This class
 * is then used to translate between the two coordinate systems.
 * <p>
 * Alternatively, a flexible canvas can be created. In this case, the canvas
 * follows the screen resolution without considering the requested
 * width/height or aspect ratio.
 */
public class Canvas {

    private int preferredWidth;
    private int preferredHeight;
    private boolean flexible;

    private int screenWidth;
    private int screenHeight;
    private int offsetX;
    private int offsetY;

    private Canvas(int preferredWidth, int preferredHeight, boolean flexible) {
        Preconditions.checkArgument(preferredWidth > 0 && preferredHeight > 0,
            "Invalid canvas dimensions: " + preferredWidth + "x" + preferredHeight);

        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.flexible = flexible;

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
        return Math.round((screenWidth - offsetX) / getZoomLevel());
    }

    public int getHeight() {
        return Math.round((screenHeight - offsetY) / getZoomLevel());
    }

    public Rect getBounds() {
        return new Rect(0f, 0f, getWidth(), getHeight());
    }

    public float getZoomLevel() {
        if (flexible) {
            return 1f;
        } else {
            float horizontalZoom = (float) screenWidth / (float) preferredWidth;
            float verticalZoom = (float) screenHeight / (float) preferredHeight;
            return Math.min(horizontalZoom, verticalZoom);
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
        // Cannot use String.format or printf since those have issues in TeaVM.
        String zoom = Formatting.numberFormat(getZoomLevel(), 1);
        return getWidth() + "x" + getHeight() + " @ " + zoom + "x";
    }

    public static Canvas create(int preferredWidth, int preferredHeight) {
        return new Canvas(preferredWidth, preferredHeight, false);
    }

    public static Canvas flexible(int initialWidth, int initialHeight) {
        return new Canvas(initialWidth, initialHeight, true);
    }
}
