//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.InputDevice;

/**
 * Simple button widget with graphics and interaction entirely managed by the
 * renderer, not using the native widget. Buttons can consist of a flat
 * background or an image background, combined with a text label.
 */
public class Button extends Widget {

    private String label;

    private InputDevice input;
    private Runnable onClick;

    public Button(WidgetStyle style, String label) {
        super(style);
        this.label = label;
    }

    public Button(WidgetStyle style) {
        this(style, "");
    }

    public void setClickHandler(InputDevice input, Runnable onClick) {
        this.input = input;
        this.onClick = onClick;
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(onClick != null, "Click handler has not been set");

        Image backgroundImage = getStyle().getBackground();
        Rect bounds = Rect.around(getX(), getY(), backgroundImage.getWidth(), backgroundImage.getHeight());

        if (input.isPointerReleased(bounds)) {
            onClick.run();
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        Image backgroundImage = getStyle().getBackground();
        TTFont font = getStyle().getFont();

        graphics.drawImage(backgroundImage, getX(), getY(), null);

        if (label != null && font != null) {
            graphics.drawText(label, font, getX(), getY() + backgroundImage.getHeight() * 0.2f,
                Align.CENTER);
        }
    }
}
