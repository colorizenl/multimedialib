//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

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
 * <p>
 * Containers can optionally be given a name. This name should be used purely
 * for information purposes. Trying to look up containers by name during each
 * frame will be too slow for most applications.
 */
public class Container implements Graphic2D {

    @Getter private DisplayListLocation location;
    private String name;

    public Container(String name) {
        this.location = new DisplayListLocation(this);
        this.name = name;
    }

    public Container() {
        this(null);
    }

    public void addChild(Graphic2D child) {
        child.getLocation().attach(this);
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
        child.getLocation().detach();
    }

    public void clearChildren() {
        location.getRemovedChildren().push(location.getChildren());
        location.getChildren().clear();
        location.getAddedChildren().clear();
    }

    public Iterable<Graphic2D> getChildren() {
        return location.getChildren();
    }

    /**
     * Invokes the specified callback function for all matching graphics
     * within this container.
     *
     * @deprecated This method requires the container to iterate over all
     *             of its children, which has a performance impact if the
     *             container is large and this is done every frame. Prefer
     *             direct references to the relevant graphics over using
     *             this method.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends Graphic2D> void forEach(Class<T> type, Consumer<T> callback) {
        for (Graphic2D child : location.getChildren()) {
            if (child.getClass() == type) {
                callback.accept((T) child);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Graphic2D child : location.getChildren()) {
            child.update(deltaTime);
        }
    }

    /**
     * Returns the smallest rectangle that can contain the bounds of all
     * graphics within this container.
     */
    @Override
    public Rect getStageBounds() {
        if (location.getChildren().isEmpty()) {
            return new Rect(0f, 0f, 0f, 0f);
        }

        Rect firstChildBounds = location.getChildren().getFirst().getStageBounds();
        float x0 = firstChildBounds.x();
        float y0 = firstChildBounds.y();
        float x1 = firstChildBounds.getEndX();
        float y1 = firstChildBounds.getEndY();

        for (int i = 1; i < location.getChildren().size(); i++) {
            Rect childBounds = location.getChildren().get(i).getStageBounds();
            x0 = Math.min(x0, childBounds.x());
            y0 = Math.min(y0, childBounds.y());
            x1 = Math.max(x1, childBounds.getEndX());
            y1 = Math.max(y1, childBounds.getEndY());
        }

        return Rect.fromPoints(x0, y0, x1, y1);
    }

    @Override
    public String toString() {
        if (name == null) {
            return "Container [" + location.getChildren().size() + "]";
        } else {
            return "Container [" + name + ", " + location.getChildren().size() + "]";
        }
    }
}
