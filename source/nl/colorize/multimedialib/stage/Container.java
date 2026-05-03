//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Timer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static lombok.AccessLevel.PROTECTED;

/**
 * Stage node that does not provide any graphics, but can instead be used to
 * add additional graphics as children. Modifying the container's transform
 * will propagate to its children.
 * <p>
 * Containers can optionally have a name. This name is only used for
 * identification, it does not influence the container's appearance or
 * behavior in any way.
 * <p>
 * Children are limited to 2D graphics, since the stage has a hard separation
 * between 2D and 3D graphics. Use {@link Group} for 3D graphics.
 */
@Getter
public class Container implements StageNode2D, Iterable<StageNode2D> {

    private String name;
    @Setter(PROTECTED) private Container parent;
    @Getter(PROTECTED) private List<StageNode2D> children;
    private Transform transform;
    private Transform globalTransform;

    public Container(String name) {
        this.name = name;
        this.children = new CopyOnWriteArrayList<>();
        this.transform = new Transform();
        this.globalTransform = new Transform();
    }

    public Container() {
        this("Container");
    }

    public void addChild(StageNode2D child) {
        Preconditions.checkArgument(this != child, "Cannot attach container to itself");
        Preconditions.checkState(child.getParent() == null, "Node is already attached to container");

        switch (child) {
            case Container container -> container.setParent(this);
            case Primitive primitive -> primitive.setParent(this);
            case Sprite sprite -> sprite.setParent(this);
            case Text text -> text.setParent(this);
            default -> throw new UnsupportedOperationException("Unknown graphics type: " + child);
        }

        children.add(child);
    }

    /**
     * Convenience method that adds the specified graphics to this container,
     * then moved the graphics' position to the specified offset.
     */
    public void addChild(StageNode2D child, Point2D relativePosition) {
        addChild(child);
        child.getTransform().setPosition(relativePosition);
    }

    /**
     * Convenience method that adds the specified graphics to this container,
     * then moved the graphics' position to the specified offset.
     */
    public void addChild(StageNode2D child, float relativeX, float relativeY) {
        addChild(child);
        child.getTransform().setPosition(relativeX, relativeY);
    }

    /**
     * Convenience method that creates a new container, adds it as a child
     * to this container, then returns the created child container.
     */
    public Container addChildContainer() {
        Container child = new Container();
        addChild(child);
        return child;
    }

    public void removeChild(StageNode2D child) {
        switch (child) {
            case Container container -> container.setParent(null);
            case Primitive primitive -> primitive.setParent(null);
            case Sprite sprite -> sprite.setParent(null);
            case Text text -> text.setParent(null);
            default -> throw new UnsupportedOperationException("Unknown graphics type: " + child);
        }

        children.remove(child);
    }

    public void clearChildren() {
        children.forEach(this::removeChild);
        children.clear();
    }

    /**
     * Invokes the specified callback function for all matching graphics
     * within this container.
     */
    @SuppressWarnings("unchecked")
    public <T extends StageNode2D> void forEach(Class<T> type, Consumer<T> callback) {
        for (StageNode2D child : children) {
            if (child.getClass() == type) {
                callback.accept((T) child);
            }
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

        Rect bounds = children.iterator().next().getStageBounds();
        for (StageNode2D child : children) {
            bounds = bounds.combine(child.getStageBounds());
        }
        return bounds;
    }

    @Override
    public void animate(Timer sceneTime) {
    }

    @Override
    public Iterator<StageNode2D> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return String.format("%s [%d]", name, children.size());
    }
}
