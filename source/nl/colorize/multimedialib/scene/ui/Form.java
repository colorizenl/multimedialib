//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import lombok.Setter;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.scene.GraphicsProvider;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.util.EventQueue;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Signal;
import nl.colorize.util.Subject;
import nl.colorize.util.TranslationBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Can be used to create a simple form-based user interface that is part of
 * the native MultimediaLib application. Since its graphics are part of the
 * application, they have to be drawn by the renderer and native UI widgets
 * cannot be used.
 * <p>
 * Forms use a two-column layout, with text labels in the left column and
 * widgets in the right column, although it is also possible to have widgets
 * span the entire column width.
 * <p>
 * Form field values are based on {@link Subject}s. This allows for both
 * synchronous access to the value, while also allowing objects to subscribe
 * to be notified whenever a form field is changed. Note that subscriptions
 * cannot outlive the form itself, which is limited to the currently active
 * scene.
 */
public class Form implements Actor, GraphicsProvider {

    private InputDevice input;
    @Setter private TranslationBundle bundle;
    private int formWidth;
    @Setter private int rowHeight;
    @Setter private int gapX;
    @Setter private int gapY;

    private Container container;
    private List<Widget> widgets;
    private List<Actor> eventPipes;

    private static final Logger LOGGER = LogHelper.getLogger(Form.class);

    public Form(InputDevice input, int formWidth, int rowHeight) {
        Preconditions.checkArgument(formWidth > 0, "Invalid form width: " + formWidth);
        Preconditions.checkArgument(rowHeight > 0, "Invalid row height: " + rowHeight);

        this.input = input;
        this.formWidth = formWidth;
        this.rowHeight = rowHeight;
        this.gapX = 0;
        this.gapY = 0;

        container = new Container("Form");
        widgets = new ArrayList<>();
        eventPipes = new ArrayList<>();
    }

    public void setGap(int gapX, int gapY) {
        Preconditions.checkArgument(gapX >= 0, "Invalid X-gap: " + gapX);
        Preconditions.checkArgument(gapY >= 0, "Invalid Y-gap: " + gapY);

        this.gapX = gapX;
        this.gapY = gapY;
    }

    /**
     * Calculates the position and size of the next widget, based on the
     * current contents of this form. Marking a widget as "full width"
     * will always make it occupy an entire (new) row by itself.
     *
     * @param columns The number of columns that should be occupied by the
     *                new widget. A value of zero means the widget's width
     *                is flexible.
     */
    private Region prepareNextWidgetBounds(int columns) {
        int columnWidth = (formWidth - gapX) / 2;

        if (widgets.isEmpty()) {
            int width = columns == 1 ? columnWidth : formWidth;
            return new Region(0, 0, width, rowHeight);
        }

        Region lastWidgetBounds = widgets.getLast().bounds;
        boolean rowHasSpace = lastWidgetBounds.x1() <= columnWidth;

        if (rowHasSpace && columns != 2) {
            // Same row, half width.
            return new Region(columnWidth + gapX, lastWidgetBounds.y(), columnWidth, rowHeight);
        } else if (columns == 1) {
            // New row, half width.
            return new Region(0, lastWidgetBounds.y1() + gapY, columnWidth, rowHeight);
        } else {
            // New row, full width.
            return new Region(0, lastWidgetBounds.y1() + gapY, formWidth, rowHeight);
        }
    }

    private void addWidget(Spatial2D graphics, Region bounds, Runnable clickHandler) {
        container.addChild(graphics);

        Widget widget = new Widget(bounds, clickHandler);
        widgets.add(widget);
    }

    private String translate(String label) {
        if (bundle == null) {
            return label;
        } else {
            return bundle.getString(label);
        }
    }

    public void addTitle(String title, WidgetStyle style) {
        Region bounds = prepareNextWidgetBounds(2);
        Spatial2D graphics = style.createLabel(translate(title), bounds.toRect());
        addWidget(graphics, bounds, null);
    }

    public void addLabel(String label, WidgetStyle style) {
        Region bounds = prepareNextWidgetBounds(1);
        Spatial2D graphics = style.createLabel(translate(label), bounds.toRect());
        addWidget(graphics, bounds, null);
    }

    public Subject<Void> addButton(String buttonLabel, WidgetStyle style) {
        Signal<Void> signal = Signal.of(null);
        Region bounds = prepareNextWidgetBounds(0);
        Spatial2D graphics = style.createButton(translate(buttonLabel), signal, bounds.toRect());
        addWidget(graphics, bounds, () -> {
            signal.set(null);
            // We have to force a change, since there is
            // no actual value that changes.
            signal.getChanges().next((Void) null);
        });
        return signal.getChanges();
    }

    public Subject<Boolean> addCheckbox(boolean value, WidgetStyle style) {
        Signal<Boolean> signal = Signal.of(value);
        Region bounds = prepareNextWidgetBounds(0);
        Spatial2D graphics = style.createCheckbox(signal, bounds.toRect());
        addWidget(graphics, bounds, () -> signal.set(!signal.get()));
        return signal.getChanges();
    }

    public Subject<String> addInputField(String value, WidgetStyle style) {
        Signal<String> signal = Signal.of(value);
        Region bounds = prepareNextWidgetBounds(0);
        Spatial2D graphics = style.createInputField(signal, bounds.toRect());
        addWidget(graphics, bounds, () -> {
            EventQueue<String> eventQueue = input.requestTextInput("", value);
            pipe(eventQueue, signal);
        });
        return signal.getChanges();
    }

    public Subject<String> addSelectField(String value, List<String> choices, WidgetStyle style) {
        Preconditions.checkArgument(!choices.isEmpty(), "Invalid select field choices");
        Preconditions.checkArgument(choices.contains(value), "Invalid initial choice: " + value);

        Signal<String> signal = Signal.of(value);
        Region bounds = prepareNextWidgetBounds(0);
        Spatial2D graphics = style.createSelectField(signal, choices, bounds.toRect());
        addWidget(graphics, bounds, () -> {
            int oldIndex = choices.indexOf(signal.get());
            int newIndex = oldIndex >= choices.size() - 1 ? 0 : oldIndex + 1;
            signal.set(choices.get(newIndex));
        });
        return signal.getChanges();
    }

    public void addSpacer() {
        Region bounds = prepareNextWidgetBounds(2);
        addWidget(new Container(), bounds, null);
    }

    @Override
    public Spatial2D getGraphics() {
        return container;
    }

    @Override
    public void update(double deltaTime) {
        for (Actor pipe : eventPipes) {
            pipe.update(deltaTime);
        }

        for (Widget widget : widgets) {
            if (widget.clickHandler != null && input.isPointerReleased(widget.bounds.toRect())) {
                widget.clickHandler.run();
                break;
            }
        }
    }

    private <T> void pipe(EventQueue<T> eventQueue, Signal<T> signal) {
        eventPipes.add(_ -> eventQueue.flush(
            signal::set,
            e -> LOGGER.warning("Failed to pipe form event: " + e.getMessage())
        ));
    }

    @VisibleForTesting
    protected List<Rect> getWidgetBounds() {
        return widgets.stream()
            .map(widget -> widget.bounds.toRect())
            .toList();
    }

    /**
     * Represents one of the widgets within this form. It does not retain a
     * reference to the widget's graphics, it only tracks its area and
     * interactivity so that the form can manage those centrally.
     */
    private record Widget(Region bounds, Runnable clickHandler) {
    }
}
