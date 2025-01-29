//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.scene.Timer;
import nl.colorize.util.SubscribableCollection;

import java.util.ArrayList;
import java.util.Iterator;

import static lombok.AccessLevel.PROTECTED;

/**
 * Stage node that does not provide any graphics, but can instead be used to
 * add additional graphics as children. Modifying the group's transform will
 * propagate to its children.
 * <p>
 * Groups can optionally have a name. This name is only used for
 * identification, it does not influence the group's appearance or behavior
 * in any way.
 * <p>
 * Children are limited to 3D graphics, since the stage has a hard separation
 * between 2D and 3D graphics. Use {@link Container} for 2D graphics.
 */
@Getter
public class Group implements StageNode3D, Iterable<StageNode3D> {

    private String name;
    @Getter(PROTECTED) private SubscribableCollection<StageNode3D> children;
    private Transform3D transform;
    private Transform3D globalTransform;

    public Group(String name) {
        this.name = name;
        this.children = SubscribableCollection.wrap(new ArrayList<>());
        this.transform = new Transform3D();
        this.globalTransform = new Transform3D();
    }

    public Group() {
        this("Group");
    }

    public void addChild(StageNode3D child) {
        Preconditions.checkArgument(this != child, "Cannot attach group to itself");
        children.add(child);
    }

    /**
     * Convenience method that creates a new group, adds it as a child to
     * this group, then returns the created child group.
     */
    public Group addChildGroup() {
        Group child = new Group();
        addChild(child);
        return child;
    }

    public void removeChild(StageNode3D child) {
        children.remove(child);
    }

    public void clearChildren() {
        children.clear();
    }

    @Override
    public void animate(Timer animationTimer) {
    }

    @Override
    public Iterator<StageNode3D> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return String.format("%s [%d]", name, children.size());
    }
}
