//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.util.MessageQueue;

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
 * <p>
 * Containers can optionally be given a name. This name should be used purely
 * for information purposes. Trying to look up containers by name during each
 * frame will be too slow for most applications.
 */
@Getter
public class Container implements Graphic2D {

    protected Graphic2D parent;
    private List<Graphic2D> children;
    private MessageQueue<Graphic2D> addedChildren;
    private MessageQueue<Graphic2D> removedChildren;

    private String name;
    private Transform transform;

    public Container(String name) {
        this.children = new ArrayList<>();
        this.addedChildren = new MessageQueue<>();
        this.removedChildren = new MessageQueue<>();

        this.name = name;
        this.transform = new Transform();
    }

    public Container() {
        this(null);
    }

    /**
     * Adds the specified graphic to this container. When the graphic is
     * rendered, it's transform properties will be interpreted relative to
     * its container.
     *
     * @throws IllegalStateException if the child is already part of the
     *         scene graph, but with a different parent.
     * @throws IllegalArgumentException when trying to attach the container
     *         to itself.
     */
    public void addChild(Graphic2D child) {
        Preconditions.checkArgument(!equals(child), "Cannot attach parent to itself");
        Preconditions.checkState(child.getParent() == null, "Already part of scene graph");

        switch (child) {
            case Container childContainer -> childContainer.parent = this;
            case Sprite sprite -> sprite.parent = this;
            case Primitive primitive -> primitive.parent = this;
            case Text text -> text.parent = this;
            default -> throw new UnsupportedOperationException("Unknown graphic: " + child);
        }

        children.add(child);
        addedChildren.offer(child);
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
        if (!children.remove(child)) {
            return;
        }

        switch (child) {
            case Container childContainer -> childContainer.parent = null;
            case Sprite sprite -> sprite.parent = null;
            case Primitive primitive -> primitive.parent = null;
            case Text text -> text.parent = null;
            default -> throw new UnsupportedOperationException("Unknown graphic: " + child);
        }

        removedChildren.offer(child);
        addedChildren.remove(child);
    }

    public void clearChildren() {
        for (Graphic2D child : children) {
            removedChildren.offer(child);
        }
        children.clear();
        addedChildren.flush();
    }

    public Iterable<Graphic2D> getChildren() {
        return children;
    }

    /**
     * Invokes the specified callback function for all matching graphics
     * within this container.
     */
    @SuppressWarnings("unchecked")
    public <T extends Graphic2D> void forEach(Class<T> type, Consumer<T> callback) {
        // Intentionally uses a classic for loop to be robust
        // against concurrent modification.
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getClass() == type) {
                callback.accept((T) children.get(i));
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

        Rect firstChildBounds = children.getFirst().getStageBounds();
        float x0 = firstChildBounds.x();
        float y0 = firstChildBounds.y();
        float x1 = firstChildBounds.getEndX();
        float y1 = firstChildBounds.getEndY();

        for (int i = 1; i < children.size(); i++) {
            Rect childBounds = children.get(i).getStageBounds();
            x0 = Math.min(x0, childBounds.x());
            y0 = Math.min(y0, childBounds.y());
            x1 = Math.max(x1, childBounds.getEndX());
            y1 = Math.max(y1, childBounds.getEndY());
        }

        return Rect.fromPoints(x0, y0, x1, y1);
    }

    @Override
    public void updateGraphics(Timer sceneTime) {
        // Intentionally uses a classic for loop to be robust
        // against concurrent modification.
        for (int i = 0; i < children.size(); i++) {
            children.get(i).updateGraphics(sceneTime);
        }
    }

    @Override
    public String toString() {
        if (name == null || name.isEmpty()) {
            return "Container [" + children.size() + "]";
        } else {
            return "Container [" + name + ", " + children.size() + "]";
        }
    }
}
