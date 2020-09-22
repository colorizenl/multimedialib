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

import java.util.List;
import java.util.function.Consumer;

/**
 * Simple select box widget with graphics and interaction entirely managed by
 * the renderer, not using the native widget.
 */
public class SelectBox extends Widget {

    private List<String> items;
    private String selected;

    private InputDevice input;
    private Consumer<String> onChange;

    public SelectBox(WidgetStyle style, List<String> items, String selected) {
        super(style);

        Preconditions.checkArgument(items.size() > 0, "No items provided");
        Preconditions.checkArgument(items.contains(selected),
            "Selected item is not included in the list of items");

        this.items = items;
        this.selected = selected;
    }

    public SelectBox(WidgetStyle style, List<String> items) {
        this(style, items, items.get(0));
    }

    public void setClickHandler(InputDevice input, Consumer<String> onChange) {
        this.input = input;
        this.onChange = onChange;
    }

    private void selectItem(int delta) {
        int index = items.indexOf(selected) + delta;
        if (index < 0) {
            index = items.size() - 1;
        } else if (index >= items.size()) {
            index = 0;
        }

        selected = items.get(index);

        if (onChange != null) {
            onChange.accept(selected);
        }
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkArgument(input != null, "Input handler not set");

        Image backgroundImage = getStyle().getBackground();

        Rect previousButtonBounds = Rect.around(getX() + backgroundImage.getWidth() * 0.3f, getY(),
            backgroundImage.getWidth() * 0.1f, backgroundImage.getHeight());
        Rect nextButtonBounds = Rect.around(getX() + backgroundImage.getWidth() * 0.4f, getY(),
            backgroundImage.getWidth() * 0.1f, backgroundImage.getHeight());

        if (input.isPointerReleased(previousButtonBounds)) {
            selectItem(-1);
        } else if (input.isPointerReleased(nextButtonBounds)) {
            selectItem(1);
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        Image backgroundImage = getStyle().getBackground();
        TTFont font = getStyle().getFont();

        graphics.drawImage(backgroundImage, getX(), getY());

        float textY = getY() + backgroundImage.getHeight() * 0.2f;
        graphics.drawText(selected, font, getX() - backgroundImage.getWidth() * 0.4f, textY);
        graphics.drawText("-", font, getX() + backgroundImage.getWidth() * 0.3f, textY, Align.CENTER);
        graphics.drawText("+", font, getX() + backgroundImage.getWidth() * 0.4f, textY, Align.CENTER);
    }

    public String getSelected() {
        return selected;
    }
}
