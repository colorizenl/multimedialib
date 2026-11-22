//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Region;

import java.util.List;

/**
 * Controls the location of widgets within a {@link Form}. The layout is
 * decided when the form is created, dynamically resizing widgets is not
 * supported.
 */
@FunctionalInterface
public interface FormLayout {

    /**
     * Calculates and returns the bounds (position and size) for the next
     * widget that will be added to the specified form. The result depends
     * on both the form's current state and this {@link FormLayout}'s
     * characteristics.
     *
     * @param requestedSpaces The widget's requested size, defined in "spaces",
     *                        where the definition of "spaces" depends on this
     *                        {@link FormLayout}. A value of 1 indicates normal
     *                        widget size, a value of 2 indicates double size,
     *                        and so on. A value of 0 indicates the widget has
     *                        no preferred size. Note the {@link FormLayout}
     *                        may decide to overrule the request.
     *
     * @throws IllegalStateException if this {@link FormLayout} has a hard
     *         limit on the number of widgets, which would be exceeded by
     *         adding this widget.
     */
    public Region determineNextWidgetBounds(Form form, int requestedSpaces);

    /**
     * Creates a new {@link FormLayout} that uses the "classic" 1xN or 2xN
     * form layout, with labels occupying the left column and the widgets
     * occupying the right column.
     */
    public static FormLayout column(int formWidth, int rowHeight, int gapX, int gapY) {
        Preconditions.checkArgument(formWidth > 0, "Invalid form width: " + formWidth);
        Preconditions.checkArgument(rowHeight > 0, "Invalid row height: " + rowHeight);
        Preconditions.checkArgument(gapX >= 0, "Invalid X-gap: " + gapX);
        Preconditions.checkArgument(gapY >= 0, "Invalid Y-gap: " + gapY);

        return (form, requestedSpaces) -> {
            List<Region> existingWidgets = form.getWidgetBounds();
            int columnWidth = (formWidth - gapX) / 2;

            if (existingWidgets.isEmpty()) {
                int width = requestedSpaces == 1 ? columnWidth : formWidth;
                return new Region(0, 0, width, rowHeight);
            }

            Region lastWidgetBounds = existingWidgets.getLast();
            boolean rowHasSpace = lastWidgetBounds.x1() <= columnWidth;

            if (rowHasSpace && requestedSpaces != 2) {
                // Same row, half width.
                return new Region(columnWidth + gapX, lastWidgetBounds.y(), columnWidth, rowHeight);
            } else if (requestedSpaces == 1) {
                // New row, half width.
                return new Region(0, lastWidgetBounds.y1() + gapY, columnWidth, rowHeight);
            } else {
                // New row, full width.
                return new Region(0, lastWidgetBounds.y1() + gapY, formWidth, rowHeight);
            }
        };
    }

    /**
     * Creates a new {@link FormLayout} that positions widgets in a horizontal
     * line, occupying a fixed amount of space. The available width is then
     * distributed equally to the widgets.
     */
    public static FormLayout distributeHorizontal(int formWidth, int height, int gapX, int widgets) {
        Preconditions.checkArgument(widgets > 0, "Invalid number of widgets: " + widgets);
        Preconditions.checkArgument(formWidth > 0, "Invalid form width: " + formWidth);
        Preconditions.checkArgument(height > 0, "Invalid form height: " + height);
        Preconditions.checkArgument(gapX >= 0, "Invalid X-gap: " + gapX);

        return (form, _) -> {
            List<Region> existingWidgets = form.getWidgetBounds();
            Preconditions.checkState(existingWidgets.size() < widgets, "Max widgets exceeded");
            int widgetWidth = (formWidth - gapX * (widgets - 1)) / widgets;
            int widgetX = existingWidgets.size() * (widgetWidth + gapX);
            return new Region(widgetX, 0, widgetWidth, height);
        };
    }

    /**
     * Creates a new {@link FormLayout} that positions widgets in a horizontal
     * line, where each widget is assigned the same width and height. The
     * total form width is therefore flexible and based on the number of
     * widgets.
     */
    public static FormLayout horizontal(int widgetWidth, int widgetHeight, int gapX) {
        Preconditions.checkArgument(widgetWidth > 0, "Invalid widget width: " + widgetWidth);
        Preconditions.checkArgument(widgetHeight > 0, "Invalid widget height: " + widgetHeight);
        Preconditions.checkArgument(gapX >= 0, "Invalid X-gap: " + gapX);

        return (form, _) -> {
            List<Region> existingWidgets = form.getWidgetBounds();
            int widgetX = existingWidgets.size() * (widgetWidth + gapX);
            return new Region(widgetX, 0, widgetWidth, widgetHeight);
        };
    }
}
