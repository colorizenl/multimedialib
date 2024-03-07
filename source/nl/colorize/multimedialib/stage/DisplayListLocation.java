//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Attached to a {@link Graphic2D} and used by the renderer to determine when
 * and how this graphic should be displayed.
 */
@Getter
public final class DisplayListLocation {

    private UUID id;
    private Graphic2D graphic;
    private Transform localTransform;
    private Transform globalTransform;

    private Graphic2D parent;
    private List<Graphic2D> children;
    private Buffer<Graphic2D> addedChildren;
    private Buffer<Graphic2D> removedChildren;

    protected DisplayListLocation(Graphic2D graphic) {
        this.id = UUID.randomUUID();
        this.graphic = graphic;
        this.localTransform = new Transform();
        this.globalTransform = new Transform();

        this.parent = null;
        this.children = new ArrayList<>();
        this.addedChildren = new Buffer<>();
        this.removedChildren = new Buffer<>();
    }

    /**
     * Attaches this {@link DisplayListLocation} to the specified parent
     * in the display list.
     *
     * @throws IllegalStateException when already attached to a parent.
     * @throws IllegalArgumentException when trying to attach to itself.
     */
    protected void attach(Graphic2D newParent) {
        Preconditions.checkNotNull(newParent, "Cannot attach to null parent");
        Preconditions.checkArgument(!newParent.equals(graphic), "Cannot attach to itself");
        Preconditions.checkState(parent == null, "Already attached to parent");

        parent = newParent;
        parent.getLocation().children.add(graphic);
        parent.getLocation().addedChildren.push(graphic);
    }

    /**
     * Detaches this {@link DisplayListLocation}, effectively removing the
     * attached graphic from the stage. If the graphics are already detached
     * this method does nothing.
     */
    public void detach() {
        if (parent != null && parent.getLocation().children.remove(graphic)) {
            parent.getLocation().addedChildren.remove(graphic);
            parent.getLocation().removedChildren.push(graphic);
            parent = null;
        }
    }

    /**
     * Returns the current <em>global transform</em>, which is relative to the
     * stage. See {@link Graphic2D#getGlobalTransform()} for more information.
     */
    public Transformable getGlobalTransform() {
        globalTransform.set(localTransform);
        if (parent != null) {
            globalTransform.combine((Transform) parent.getLocation().getGlobalTransform());
        }
        return globalTransform;
    }
}
