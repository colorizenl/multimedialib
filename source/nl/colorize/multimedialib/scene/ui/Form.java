//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.InputDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel that contains a number of user interface widgets that are layed out
 * in a two-dimensional grid. The location of the widgets will be determined
 * automatically relative to the location of the form. The form's coordinates
 * describe the X coordinate of its center, and the Y coordinate of the first
 * row of widgets.
 */
public class Form extends Widget {

    private List<WidgetInfo> widgets;
    private InputDevice input;
    private WidgetStyle labelStyle;
    private float horizontalMargin;
    private float verticalMargin;
    private float labelOffset;

    public Form(Location location, WidgetStyle labelStyle, InputDevice input) {
        super(null);
        setLocation(location);

        this.widgets = new ArrayList<>();
        this.labelStyle = labelStyle;
        this.input = input;
        this.horizontalMargin = 10f;
        this.verticalMargin = 20f;
        this.labelOffset = 0f;
    }

    private void addWidget(String label, Widget widget) {
        WidgetInfo widgetInfo = new WidgetInfo(widget);

        if (label != null && !label.isEmpty()) {
            widgetInfo.label = new TextLabel(labelStyle, label, Align.RIGHT);
        }

        widgets.add(widgetInfo);
        updateLayout();
    }

    public void add(TextLabel label) {
        addWidget("", label);
    }

    public Button add(String label, Button button, Runnable onClick) {
        addWidget(label, button);
        button.setClickHandler(input, onClick);
        return button;
    }

    public Button add(Button button, Runnable onClick) {
        return add("", button, onClick);
    }

    public SelectBox add(String label, SelectBox selectBox, Consumer<String> onChange) {
        addWidget(label, selectBox);
        selectBox.setClickHandler(input, onChange);
        return selectBox;
    }

    public SelectBox add(String label, SelectBox selectBox, Runnable onChange) {
        Consumer<String> onChangeWrapper = value -> onChange.run();
        return add(label, selectBox, onChangeWrapper);
    }

    public SelectBox add(String label, SelectBox selectBox) {
        return add(label, selectBox, (Consumer<String>) null);
    }

    public TextField add(String label, TextField textField, Consumer<String> onChange) {
        addWidget(label, textField);
        textField.setChangeHandler(input, onChange);
        return textField;
    }

    public TextField add(String label, TextField textField) {
        return add(label, textField, null);
    }

    public void addEmptyRow() {
        addWidget("", new TextLabel(labelStyle, ""));
    }

    public void setVisible(Widget widget, boolean visible) {
        for (WidgetInfo widgetInfo : widgets) {
            if (widgetInfo.widget.equals(widget)) {
                widgetInfo.visible = visible;
                break;
            }
        }

        updateLayout();
    }

    public void setMargin(float horizontalMargin, float verticalMargin) {
        this.horizontalMargin = horizontalMargin;
        this.verticalMargin = verticalMargin;
    }

    public void setLabelOffset(float labelOffset) {
        this.labelOffset = labelOffset;
    }

    @Override
    public void update(float deltaTime) {
        for (WidgetInfo widgetInfo : widgets) {
            widgetInfo.widget.update(deltaTime);
        }

        updateLayout();
    }

    private void updateLayout() {
        Point2D cursor = new Point2D(0f, 0f);
        Location parent = getLocation();

        for (WidgetInfo widgetInfo : widgets) {
            if (widgetInfo.visible) {
                if (widgetInfo.label != null) {
                    widgetInfo.label.setLocation(parent.relativeTo(-horizontalMargin / 2f,
                        cursor.getY() + labelOffset));
                }

                widgetInfo.widget.setLocation(parent.relativeTo(getWidgetX(widgetInfo), cursor.getY()));

                cursor.setY(cursor.getY() + verticalMargin);
            }
        }
    }

    private float getWidgetX(WidgetInfo widgetInfo) {
        WidgetStyle style = widgetInfo.widget.getStyle();
        if (style == null || style.getBackground() == null) {
            return horizontalMargin / 2f;
        }
        return horizontalMargin / 2f + style.getBackground().getWidth() / 2f;
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        for (WidgetInfo widgetInfo : widgets) {
            if (widgetInfo.visible) {
                if (widgetInfo.label != null) {
                    widgetInfo.label.render(graphics);
                }
                widgetInfo.widget.render(graphics);
            }
        }
    }

    @Override
    public WidgetStyle getStyle() {
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    protected List<Widget> getWidgets() {
        List<Widget> result = new ArrayList<>();

        for (WidgetInfo widgetInfo : widgets) {
            if (widgetInfo.visible) {
                if (widgetInfo.label != null) {
                    result.add(widgetInfo.label);
                }
                result.add(widgetInfo.widget);
            }
        }

        return result;
    }

    /**
     * Information related to how a widget is displayed within the form.
     */
    private static class WidgetInfo {

        private Widget widget;
        private TextLabel label;
        private boolean visible;

        public WidgetInfo(Widget widget) {
            this.widget = widget;
            this.visible = true;
        }
    }
}
