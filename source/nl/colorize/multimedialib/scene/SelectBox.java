//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.GraphicsContext;
import nl.colorize.multimedialib.renderer.InputDevice;

import java.util.List;
import java.util.function.Consumer;

/**
 * Simple select box widget with graphics and interaction entirely managed by
 * the renderer, not using the native widget.
 */
public class SelectBox implements Subsystem {

    private Rect bounds;
    private ColorRGB background;
    private TTFont font;
    private List<String> items;
    private String selected;

    private Consumer<String> onChange;
    private Button previousButton;
    private Button nextButton;

    public SelectBox(Rect bounds, List<String> items, String selected, ColorRGB background, TTFont font) {
        Preconditions.checkArgument(items.size() > 0, "No items provided");
        Preconditions.checkArgument(items.contains(selected),
            "Selected item is not included in the list of items");

        this.bounds = bounds;
        this.items = items;
        this.selected = selected;
        this.background = background;
        this.font = font;
    }

    public void setClickHandler(InputDevice input, Consumer<String> onChange) {
        this.onChange = onChange;

        Rect previousButtonBounds = new Rect(bounds.getEndX() - 2f * bounds.getHeight(),
            bounds.getY(), bounds.getHeight(), bounds.getHeight());
        previousButton = new Button(previousButtonBounds, background, "-", font);
        previousButton.setClickHandler(input, () -> selectItem(-1));

        Rect nextButtonBounds = new Rect(bounds.getEndX() - bounds.getHeight(), bounds.getY(),
            bounds.getHeight(), bounds.getHeight());
        nextButton = new Button(nextButtonBounds, background, "+", font);
        nextButton.setClickHandler(input, () -> selectItem(1));
    }

    private void selectItem(int delta) {
        int index = items.indexOf(selected) + delta;
        if (index < 0) {
            index = items.size() - 1;
        } else if (index >= items.size()) {
            index = 0;
        }

        selected = items.get(index);
        onChange.accept(selected);
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(onChange != null, "Change handler has not been set");

        previousButton.update(deltaTime);
        nextButton.update(deltaTime);
    }

    @Override
    public void render(GraphicsContext graphics) {
        graphics.drawRect(bounds, background, null);
        graphics.drawText(selected, font, bounds.getX() + bounds.getWidth() * 0.1f,
            bounds.getY() + bounds.getHeight() * 0.7f, Align.CENTER);

        previousButton.render(graphics);
        nextButton.render(graphics);
    }
}
