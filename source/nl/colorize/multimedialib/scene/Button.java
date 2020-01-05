//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;

/**
 * Simple button widget with graphics and interaction entirely managed by the
 * renderer, not using the native widget. Buttons can consist of a flat
 * background or an image background, combined with a text label.
 */
public class Button implements Subsystem {

    private Rect bounds;
    private ColorRGB backgroundColor;
    private Image backgroundImage;
    private String label;
    private TTFont font;

    private InputDevice input;
    private Runnable onClick;

    public Button(Rect bounds, ColorRGB background, String label, TTFont font) {
        this.bounds = bounds;
        this.backgroundColor = background;
        this.label = label;
        this.font = font;
    }

    public Button(Rect bounds, Image background, String label, TTFont font) {
        this.bounds = bounds;
        this.backgroundImage = background;
        this.label = label;
        this.font = font;
    }

    public Button(Rect bounds, Image background) {
        this(bounds, background, null, null);
    }

    public void setClickHandler(InputDevice input, Runnable onClick) {
        this.input = input;
        this.onClick = onClick;
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(onClick != null, "Click handler has not been set");

        if (input.isPointerReleased() && bounds.contains(input.getPointer())) {
            onClick.run();
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        if (backgroundImage != null) {
            graphics.drawImage(backgroundImage, bounds.getCenterX(), bounds.getCenterY(), null);
        } else if (backgroundColor != null) {
            graphics.drawRect(bounds, backgroundColor, null);
        }

        if (label != null && font != null) {
            graphics.drawText(label, font, bounds.getCenterX(),
                bounds.getY() + bounds.getHeight() * 0.7f, Align.CENTER);
        }
    }
}
