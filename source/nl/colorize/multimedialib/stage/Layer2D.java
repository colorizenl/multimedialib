//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.scene.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Access to one of the 2D graphics layers on the stage. The graphics in this
 * layer will appear "on top" or "below" other layers, depending on the order
 * of layers on the stage. If 3D graphics are used, the 2D graphics are always
 * displayed "on top" of the 3D graphics.
 */
public class Layer2D implements Updatable {

    private String name;
    private List<Graphic2D> graphics;
    private List<StageObserver> observers;

    public static final String DEFAULT_LAYER = "$$default";

    protected Layer2D(String name, List<StageObserver> observers) {
        this.name = name;
        this.graphics = new ArrayList<>();
        this.observers = observers;

        observers.forEach(observer -> observer.onLayerAdded(this));
    }

    public String getName() {
        return name;
    }

    public void add(Graphic2D... contents) {
        for (Graphic2D graphic : contents) {
            graphics.add(graphic);
            observers.forEach(observer -> observer.onGraphicAdded(this, graphic));
        }
    }

    public void add(Group group) {
        for (Graphic2D graphic : group) {
            add(graphic);
        }
    }

    public void remove(Graphic2D graphic) {
        graphics.remove(graphic);
        observers.forEach(observer -> observer.onGraphicRemoved(this, graphic));
    }

    public void remove(Group group) {
        for (Graphic2D graphic : group) {
            remove(graphic);
        }
    }

    public Iterable<Graphic2D> getGraphics() {
        return graphics;
    }

    @Override
    public void update(float deltaTime) {
        for (Graphic2D graphic : graphics) {
            graphic.update(deltaTime);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
