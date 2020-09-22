//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.renderer.Drawable;
import nl.colorize.multimedialib.renderer.Updatable;

/**
 * Base class for all subsystems that emulate user interface widgets
 * (buttons, select boxes, text fields, etc.). This does not actually use the
 * platform's native user interface, but implements graphics and interaction
 * entirely in MultimediaLib so that the widgets works across all platforms.
 */
public abstract class Widget implements Updatable, Drawable {

    private boolean visible;
    private Location location;
    private WidgetStyle style;

    public Widget(WidgetStyle style) {
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
}
