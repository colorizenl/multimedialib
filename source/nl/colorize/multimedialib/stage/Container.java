//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

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
 * <p>
 * Containers keep track of added and removed children. This information can
 * be retrieved in two ways: by registering observers that are notified
 * whenever a child is added or removed, or via a queue that can be polled on
 * a frame-by-frame basis.
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
        Preconditions.checkArgument(!child.equals(this),
            "Cannot add container to itself");

        Preconditions.checkArgument(!child.getLocation().isAttached(),
            "Graphic is already attached to a different parent");

        child.getLocation().attach(this);
        children.add(child);
        addedChildren.push(child);
    }

    /**
     * Convenience method that adds the specified graphics to this container,
     * then moved the graphics' position to the specified offset.
     */
    public void addChild(Graphic2D child, Point2D relativePosition) {
        addChild(child);
        child.getTransform().setPosition(relativePosition);
    }

    /**
     * Convenience method that adds the specified graphics to this container,
     * then moved the graphics' position to the specified offset.
     */
    public void addChild(Graphic2D child, float relativeX, float relativeY) {
        addChild(child);
        child.getTransform().setPosition(relativeX, relativeY);
    }

    /**
     * Convenience method that creates a child container, adds it as a child
     * to this container, then returns the created child container.
     */
    public Container addChildContainer() {
        Container child = new Container();
        addChild(child);
        return child;
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
        addedChildren.flush();
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

    /**
     * Returns the smallest rectangle that can contain the bounds of all
     * graphics within this container.
     */
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
