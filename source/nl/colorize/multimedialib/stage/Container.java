//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.InteractiveObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Display object that acts as a container for holding 2D graphics. Children
 * are visited in the order they were added, which means children further down
 * the list will be drawn "on top of" children earlier in the list.
 * <p>
 * Containers have their own {@link Transform}, and this is inherited by their
 * children. In other words, the child display object's position is relative
 * to its parent container's position, not relative to the stage.
 */
public class Container implements Graphic2D {

    @Getter private StageLocation location;
    private List<Graphic2D> children;
    @Getter private Buffer<Graphic2D> addedChildren;
    @Getter private Buffer<Graphic2D> removedChildren;

    public Container() {
        this.location = new StageLocation();
        this.children = new ArrayList<>();
        this.addedChildren = new Buffer<>();
        this.removedChildren = new Buffer<>();
    }

    public void addChild(Graphic2D child) {
        child.getLocation().attach(this);
        children.add(child);
        addedChildren.push(child);
    }

    public void addChild(Graphic2D child, Point2D relativePosition) {
        addChild(child);
        child.getTransform().setPosition(relativePosition);
    }

    public void addChild(Graphic2D child, float relativeX, float relativeY) {
        addChild(child);
        child.getTransform().setPosition(relativeX, relativeY);
    }

    public void addChild(InteractiveObject object) {
        addChild(object.getContainer());
    }

    public void removeChild(Graphic2D child) {
        if (children.remove(child)) {
            child.getLocation().detach();
            removedChildren.push(child);
        }
    }

    public void clearChildren() {
        children.forEach(removedChildren::push);
        children.clear();
        addedChildren.clear();
    }

    public Iterable<Graphic2D> getChildren() {
        return children;
    }

    public void forEach(Consumer<Graphic2D> callback) {
        children.forEach(callback);
    }

    public <T extends Graphic2D> void forEach(Class<T> type, Consumer<T> callback) {
        for (Graphic2D child : children) {
            if (child.getClass() == type) {
                callback.accept((T) child);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Graphic2D child : children) {
            child.update(deltaTime);
        }
    }

    @Override
    public Rect getStageBounds() {
        if (children.isEmpty()) {
            return new Rect(0f, 0f, 0f, 0f);
        }

        float x0 = Integer.MAX_VALUE;
        float y0 = Integer.MAX_VALUE;
        float x1 = Integer.MIN_VALUE;
        float y1 = Integer.MIN_VALUE;

        for (Graphic2D child : children) {
            Rect childBounds = child.getStageBounds();
            x0 = Math.min(x0, childBounds.getX());
            y0 = Math.min(y0, childBounds.getY());
            x1 = Math.max(x1, childBounds.getEndX());
            y1 = Math.max(y1, childBounds.getEndY());
        }

        return Rect.fromPoints(x0, y0, x1, y1);
    }

    @Override
    public String toString() {
        return "Container";
    }
}