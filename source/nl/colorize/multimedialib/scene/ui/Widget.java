//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.graphics.GraphicsLayer2D;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.scene.Agent;

/**
 * Base class for all subsystems that emulate user interface widgets
 * (buttons, select boxes, text fields, etc.). This does not actually use the
 * platform's native user interface, but implements graphics and interaction
 * entirely in MultimediaLib so that the widgets works across all platforms.
 */
public abstract class Widget implements Agent, GraphicsLayer2D {

    private boolean visible;
    private Location location;
    private WidgetStyle style;

    public Widget(WidgetStyle style) {
        Preconditions.checkArgument(style != null, "Widget style is required");

        this.visible = true;
        this.location = Location.fixed(0f, 0f);
        this.style = style;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    protected float getX() {
        return location.getX();
    }

    protected float getY() {
        return location.getY();
    }

    public WidgetStyle getStyle() {
        return style;
    }

    @Override
    public final void render(GraphicsContext2D graphics) {
        if (style.getBackgroundGraphics() != null) {
            style.getBackgroundGraphics().render(graphics);
        }

        render(graphics, style);
    }

    protected abstract void render(GraphicsContext2D graphics, WidgetStyle style);

    @Override
    public final boolean isCompleted() {
        return false;
    }
}
