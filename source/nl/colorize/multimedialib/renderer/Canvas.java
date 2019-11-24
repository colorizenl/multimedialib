//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;

/**
 * Used to control the size of the canvas displaying the application's graphics,
 * independent from the size of the screen. This allows the application to control
 * the zoom level, therefore controlling the user experience in situations where
 * the same application is used across a variety of devices with different screen
 * sizes. This class allows to translate between the two coordinate systems.
 * <p>
 * Although the canvas is given a preferred size and aspect ratio upon creation,
 * it is not guaranteed that these will ever be used. This is especially true for
 * mobile apps, where the application will usually fill the entire screen.
 */
public class Canvas {

    private int preferredWidth;
    private int preferredHeight;
    private float preferredZoomLevel;

    private int screenWidth;
    private int screenHeight;
    private int offsetX;
    private int offsetY;

    public Canvas(int preferredWidth, int preferredHeight, float zoomLevel) {
        Preconditions.checkArgument(preferredWidth > 0 && preferredHeight > 0,
            "Invalid canvas dimensions: " + preferredWidth + "x" + preferredHeight);
        Preconditions.checkArgument(zoomLevel > 0f, "Invalid zoom level: " + zoomLevel);

        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.preferredZoomLevel = zoomLevel;

        this.screenWidth = preferredWidth;
        this.screenHeight = preferredHeight;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    public Canvas(int preferredWidth, int preferredHeight) {
        this(preferredWidth, preferredHeight, 1f);
    }

    /**
     * Sets the screen dimensions to the specified values. This method should be
     * called by the renderer when the application window is first created, and
     * whenever the window is resized.
     */
    public void resize(int screenWidth, int screenHeight) {
        Preconditions.checkArgument(screenWidth > 0 && screenHeight > 0,
            "Invalid screen dimensions: " + screenWidth + "x" + screenHeight);

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void offset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public int getWidth() {
        return Math.round((screenWidth - offsetX) / getZoomLevel());
    }

    public int getHeight() {
        return Math.round((screenHeight - offsetY) / getZoomLevel());
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public float getZoomLevel() {
        return preferredZoomLevel;
    }

    public int toCanvasX(int screenX) {
        return Math.round((screenX - offsetX) / getZoomLevel());
    }

    public int toCanvasY(int screenY) {
        return Math.round((screenY - offsetY) / getZoomLevel());
    }

    public int toScreenX(float canvasX) {
        return Math.round((canvasX * getZoomLevel()) + offsetX);
    }

    public int toScreenY(float canvasY) {
        return Math.round((canvasY * getZoomLevel()) + offsetY);
    }
}
