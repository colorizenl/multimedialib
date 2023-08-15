//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

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

    public float getZoomLevel(Canvas canvas);

    /**
     * Does not perform any scaling, and makes the canvas match the native
     * screen resolution and aspect ratio.
     */
    public static ScaleStrategy flexible() {
        return canvas -> 1f;
    }

    /**
     * Will try to match the preferred size, scaling the canvas if necessary.
     * This will generally produce better-looking results than {@link #fit()}.
     * The downside is the canvas might not fit within the screen bounds when
     * the actual aspect ratio is different from the preferred aspect ratio.
     */
    public static ScaleStrategy scale() {
        return canvas -> {
            float horizontal = (float) canvas.getScreenWidth() / (float) canvas.getPreferredWidth();
            float vertical = (float) canvas.getScreenHeight() / (float) canvas.getPreferredHeight();
            return Math.max(horizontal, vertical);
        };
    }

    /**
     * Will zoom out until it is able to fit its preferred size. Compared to
     * {@link #scale()}, this ensures the entire canvas is visible at all
     * times. The downside is that the canvas will appear extremely zoomed
     * out when the actual aspect ratio is different from the preferred
     * aspect ratio.
     */
    public static ScaleStrategy fit() {
        return canvas -> {
            float horizontal = (float) canvas.getScreenWidth() / (float) canvas.getPreferredWidth();
            float vertical = (float) canvas.getScreenHeight() / (float) canvas.getPreferredHeight();
            return Math.min(horizontal, vertical);
        };
    }

    /**
     * Tries to find a balance between the behavior of {@link #fit()} and
     * {@link #scale()}.
     */
    public static ScaleStrategy balanced() {
        return canvas -> {
            float horizontal = (float) canvas.getScreenWidth() / (float) canvas.getPreferredWidth();
            float vertical = (float) canvas.getScreenHeight() / (float) canvas.getPreferredHeight();
            return (horizontal + vertical) / 2f;
        };
    }
}
