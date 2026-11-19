//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.multimedialib.scene.EventSource;
import nl.colorize.multimedialib.scene.GraphicsProvider;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Transform;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Subject;
import nl.colorize.util.TranslationBundle;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static nl.colorize.multimedialib.stage.ColorRGB.PINK;

/**
 * Can be used to create a simple form-based user interface that is part of
 * the native MultimediaLib application. Since its graphics are part of the
 * application, they have to be drawn by the renderer and native UI widgets
 * cannot be used.
 * <p>
 * The form's layout is controlled by {@link FormLayout}. The layout is
 * decided when the form is created, dynamically resizing widgets is not
 * supported. Each widget's visual appearance is controlled by
 * {@link WidgetStyle}. This is not a global configuration, it's possible to
 * use different styles for different widgets.
 * <p>
 * Form field values are based on {@link Subject}s. This allows for both
 * synchronous access to the value, while also allowing objects to subscribe
 * to be notified whenever a form field is changed. Note that subscriptions
 * cannot outlive the form itself, which is limited to the currently active
 * scene.
 */
public class Form implements Actor, GraphicsProvider {

    private InputDevice input;
    private FormLayout layout;
    private TranslationBundle bundle;
    @Getter private int formWidth;
    @Getter private int formHeight;

    private Container container;
    private List<Widget> widgets;
    private List<Actor> eventPipes;

    private static final boolean BOUNDS = "true".equals(System.getProperty("multimedialib.formbounds"));
    private static final Logger LOGGER = LogHelper.getLogger(Form.class);

    public Form(InputDevice input, FormLayout layout, @Nullable TranslationBundle bundle) {
        this.input = input;
        this.layout = layout;
        this.bundle = bundle;

        container = new Container("Form");
        widgets = new ArrayList<>();
        eventPipes = new ArrayList<>();
    }

    private void addWidget(InputModel<?> model, Spatial2D graphics, Region bounds, Runnable click) {
        container.addChild(graphics);

        Widget widget = new Widget(model, bounds, click);
        widgets.add(widget);

        formWidth = Math.max(formWidth, bounds.x1());
        formHeight = Math.max(formHeight, bounds.y1());

        if (BOUNDS) {
            Primitive outline = new Primitive(SegmentedLine.fromOutline(bounds.toRect()), PINK);
            outline.setStroke(2);
            container.addChild(outline);
        }
    }

    private String translate(String label) {
        if (bundle == null) {
            return label;
        }
        return bundle.getString(label);
    }

    public void addTitle(String title, WidgetStyle style) {
        InputModel<String> model = new InputModel<>(title);
        Region bounds = layout.determineNextWidgetBounds(this, 2);
        Spatial2D graphics = style.createTitle(translate(title), bounds.toRect());
        addWidget(model, graphics, bounds, null);
    }

    public void addLabel(String label, WidgetStyle style) {
        InputModel<String> model = new InputModel<>(label);
        Region bounds = layout.determineNextWidgetBounds(this, 1);
        Spatial2D graphics = style.createLabel(translate(label), bounds.toRect());
        addWidget(model, graphics, bounds, null);
    }

    public InputModel<Void> addButton(String buttonLabel, WidgetStyle style) {
        InputModel<Void> model = new InputModel<>(null);
        Region bounds = layout.determineNextWidgetBounds(this, 0);
        Spatial2D graphics = style.createButton(translate(buttonLabel), model, bounds.toRect());
        addWidget(model, graphics, bounds, () -> {
            // We have to force a change, since there is
            // no actual value that changes.
            model.getValue().getChanges().next((Void) null);
        });
        return model;
    }

    public InputModel<Boolean> addCheckbox(String label, boolean value, WidgetStyle style) {
        InputModel<Boolean> model = new InputModel<>(value);
        Region bounds = layout.determineNextWidgetBounds(this, 0);
        Spatial2D graphics = style.createCheckbox(translate(label), model, bounds.toRect());
        addWidget(model, graphics, bounds, () -> model.getValue().set(!model.getValue().get()));
        return model;
    }

    public InputModel<String> addInputField(String value, WidgetStyle style) {
        InputModel<String> model = new InputModel<>(value);
        Region bounds = layout.determineNextWidgetBounds(this, 0);
        Spatial2D graphics = style.createInputField(model, bounds.toRect());
        addWidget(model, graphics, bounds, () -> {
            EventSource<String> inputSource = input.requestTextInput("", value);
            pipe(inputSource, model);
        });
        return model;
    }

    public InputModel<String> addSelectField(String value, List<String> choices, WidgetStyle style) {
        Preconditions.checkArgument(!choices.isEmpty(), "Invalid select field choices");
        Preconditions.checkArgument(choices.contains(value), "Invalid initial choice: " + value);

        InputModel<String> model = new InputModel<>(value);
        Region bounds = layout.determineNextWidgetBounds(this, 0);
        Spatial2D graphics = style.createSelectField(model, choices, bounds.toRect());
        addWidget(model, graphics, bounds, () -> {
            int oldIndex = choices.indexOf(model.getValue().get());
            int newIndex = oldIndex >= choices.size() - 1 ? 0 : oldIndex + 1;
            model.getValue().set(choices.get(newIndex));
        });
        return model;
    }

    public void addSpacer() {
        InputModel<String> model = new InputModel<>("");
        Region bounds = layout.determineNextWidgetBounds(this, 2);
        addWidget(model, new Container(), bounds, null);
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
            if (isClicked(widget)) {
                widget.clickHandler.run();
                break;
            }
        }
    }

    private boolean isClicked(Widget widget) {
        if (widget.clickHandler == null || !widget.model.getEnabled().get()) {
            return false;
        }

        Transform globalTransform = container.getGlobalTransform();
        if (!globalTransform.isVisible()) {
            return false;
        }

        Point2D formStagePosition = globalTransform.getPosition();
        Rect stageBounds = widget.bounds.toRect().add(formStagePosition);
        return input.isPointerReleased(stageBounds);
    }

    @SuppressWarnings("deprecation")
    private <T> void pipe(EventSource<T> eventSource, InputModel<T> model) {
        eventPipes.add(_ -> eventSource.poll(
            event -> model.getValue().set(event),
            e -> LOGGER.warning("Failed to pipe form event: " + e.getMessage())
        ));
    }

    protected List<Region> getWidgetBounds() {
        return widgets.stream()
            .map(widget -> widget.bounds)
            .toList();
    }

    /**
     * Represents one of the widgets within this form. It does not retain a
     * reference to the widget's graphics, it only tracks its area and
     * interactivity so that the form can manage those centrally.
     */
    private record Widget(InputModel<?> model, Region bounds, Runnable clickHandler) {
    }
}
