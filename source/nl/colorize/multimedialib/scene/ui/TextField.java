//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.InputDevice;

import java.util.function.Consumer;

/**
 * Simple text field widget with graphics and interaction entirely managed by
 * the renderer, not using the native widget. Actually entering text is not
 * handled by this class. Instead, it will show a dialog window so that the
 * user can enter text using the platform's native text field.
 */
public class TextField extends Widget {

    private String label;
    private String value;

    private InputDevice input;
    private Consumer<String> onChange;

    public TextField(WidgetStyle style, String label, String initialValue) {
        super(style);
        this.label = label;
        this.value = initialValue;
    }

    public TextField(WidgetStyle style, String label) {
        this(style, label, "");
    }

    public void setChangeHandler(InputDevice input, Consumer<String> onChange) {
        this.input = input;
        this.onChange = onChange;
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkArgument(input != null, "Change handler not set");

        Image backgroundImage = getStyle().getBackground();
        Rect bounds = Rect.around(getX(), getY(), backgroundImage.getWidth(), backgroundImage.getHeight());

        if (input.isPointerReleased(bounds)) {
            String enteredValue = input.requestTextInput(label, value);
            if (enteredValue != null) {
                value = enteredValue;

                if (onChange != null) {
                    onChange.accept(value);
                }
            }
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        Image backgroundImage = getStyle().getBackground();
        graphics.drawImage(backgroundImage, getX(), getY());

        graphics.drawText(value, getStyle().getFont(), getX() - backgroundImage.getWidth() * 0.4f,
            getY() + backgroundImage.getHeight() * 0.2f);
    }

    public String getValue() {
        return value;
    }
}
