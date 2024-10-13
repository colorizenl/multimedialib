//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Size;

/**
 * Determines how the {@link Canvas} should be scaled and zoomed when viewed
 * on devices with different screen sizes, resolutions, and aspect ratios.
 * This behavior is controlled by the zoom level. A zoom level of 1.0 indicates
 * the canvas should be shown at its native size. Larger values will make the
 * canvas appear "zoomed in", while smaller values will make the canvas appear
 * "zoomed out".
 */
@FunctionalInterface
public interface ScaleStrategy {

    default float getZoomLevel(Canvas canvas) {
        Size preferredSize = canvas.getPreferredSize();
        Size screenSize = canvas.getScreenSize();
        float horizontalZoom = (float) screenSize.width() / (float) preferredSize.width();
        float verticalZoom = (float) screenSize.height() / (float) preferredSize.height();
        return getZoomLevel(horizontalZoom, verticalZoom);
    }

    public float getZoomLevel(float horizontalZoom, float verticalZoom);

    /**
     * Does not perform any scaling, and makes the canvas match the native
     * screen resolution and aspect ratio.
     */
    public static ScaleStrategy flexible() {
        return (horizontalZoom, verticalZoom) -> 1f;
    }

    /**
     * Will try to match the preferred size, scaling the canvas if necessary.
     * This will generally produce better-looking results than {@link #fit()}.
     * The downside is the canvas might not fit within the screen bounds when
     * the actual aspect ratio is different from the preferred aspect ratio.
     */
    public static ScaleStrategy scale() {
        return (horizontalZoom, verticalZoom) -> Math.max(horizontalZoom, verticalZoom);
    }

    /**
     * Will zoom out until it is able to fit its preferred size. Compared to
     * {@link #scale()}, this ensures the entire canvas is visible at all
     * times. The downside is that the canvas will appear extremely zoomed
     * out when the actual aspect ratio is different from the preferred
     * aspect ratio.
     */
    public static ScaleStrategy fit() {
        return (horizontalZoom, verticalZoom) -> Math.min(horizontalZoom, verticalZoom);
    }

    /**
     * Tries to find a balance between the behavior of {@link #fit()} and
     * {@link #scale()}.
     */
    public static ScaleStrategy balanced() {
        return (horizontalZoom, verticalZoom) -> (horizontalZoom + verticalZoom) / 2f;
    }
}
