//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
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
    private int width;
    private int height;

    private InputDevice input;
    private Runnable onClick;

    public Button(WidgetStyle style, String label) {
        super(style);
        this.label = label;

        if (style.getBackground() != null) {
            width = style.getBackground().getWidth();
            height = style.getBackground().getHeight();
        }
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

        if (input.isPointerReleased(getBounds())) {
            onClick.run();
        }
    }

    private Rect getBounds() {
        return Rect.around(getX(), getY(), width, height);
    }

    @Override
    public void render(GraphicsContext2D graphics, WidgetStyle style) {
        if (style.getBorderColor() != null && style.getBorderSize() > 0) {
            drawBorder(graphics, style);
        }

        if (style.getBackground() != null) {
            graphics.drawImage(style.getBackground(), getX(), getY(), null);
        } else if (style.getBackgroundColor() != null) {
            graphics.drawRect(getBounds(), style.getBackgroundColor());
        }

        if (label != null && style.getFont() != null) {
            graphics.drawText(label, style.getFont(), getX(), getY() + height * 0.35f, Align.CENTER);
        }
    }

    private void drawBorder(GraphicsContext2D graphics, WidgetStyle style) {
        Rect bounds = getBounds();
        int borderSize = style.getBorderSize();
        Rect border = new Rect(bounds.getX() - borderSize, bounds.getY() - borderSize,
            bounds.getWidth() + 2 * borderSize, bounds.getHeight() + 2 * borderSize);

        graphics.drawRect(border, style.getBorderColor());
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
