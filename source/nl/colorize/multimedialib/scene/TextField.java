//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;

import java.util.function.Consumer;

/**
 * Simple text field widget with graphics and interaction entirely managed by
 * the renderer, not using the native widget. Actually entering text is not
 * handled by this class. Instead, it will show a dialog window so that the
 * user can enter text using the platform's native text field.
 */
public class TextField implements Subsystem {

    private Rect bounds;
    private ColorRGB background;
    private TTFont font;
    private String label;
    private String value;

    private InputDevice input;
    private Consumer<String> onChange;

    public TextField(Rect bounds, ColorRGB background, TTFont font, String label, String initialValue) {
        this.bounds = bounds;
        this.background = background;
        this.font = font;
        this.label = label;
        this.value = initialValue;
    }

    public TextField(Rect bounds, ColorRGB background, TTFont font, String label) {
        this(bounds, background, font, label, "");
    }

    public void setChangeHandler(InputDevice input, Consumer<String> onChange) {
        this.input = input;
        this.onChange = onChange;
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(onChange != null, "Change handler has not been set");

        if (input.isPointerReleased() && bounds.contains(input.getPointer())) {
            String enteredValue = input.requestTextInput(label, value);
            if (enteredValue != null) {
                value = enteredValue;
                onChange.accept(value);
            }
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        graphics.drawRect(bounds, background);
        graphics.drawText(value, font, bounds.getX() + bounds.getWidth() * 0.1f,
            bounds.getY() + bounds.getHeight() * 0.7f);
    }
}
