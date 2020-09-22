//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene implementation specifically intended for menu systems. The scene
 * consists of a number of menu widgets that are created when the scene is
 * started, and are then displayed throughout the scene.
 */
public abstract class MenuScene implements Scene {

    private List<Widget> widgets;

    public MenuScene() {
        this.widgets = new ArrayList<>();
    }

    public void add(Widget widget) {
        widgets.add(widget);
    }

    public void remove(Widget widget) {
        widgets.remove(widget);
    }

    @Override
    public abstract void start(Application app);

    @Override
    public final void update(Application app, float deltaTime) {
        Widget[] snapshot = this.widgets.toArray(new Widget[0]);
        for (Widget widget : snapshot) {
            widget.update(deltaTime);
        }
    }

    @Override
    public final void render(Application app, GraphicsContext2D graphics) {
        drawBackground(graphics);
        
        for (Widget widget : widgets) {
            widget.render(graphics);
        }
    }

    public abstract void drawBackground(GraphicsContext2D graphics);
}
