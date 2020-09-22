//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;

/**
 * Simple text label that is drawn at the specified coordinates.
 */
public class TextLabel extends Widget {

    private String text;
    private Align align;

    public TextLabel(WidgetStyle style, String text, Align align) {
        super(style);
        this.text = text;
        this.align = align;
    }

    public TextLabel(WidgetStyle style, String text) {
        this(style, text, Align.LEFT);
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        graphics.drawText(text, getStyle().getFont(), getX(), getY(), align);
    }
}
