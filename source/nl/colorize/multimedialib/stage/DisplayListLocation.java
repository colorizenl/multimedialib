//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Angle;
import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.util.stats.Aggregate;

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
        this.localTransform = new LocalTransform();
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
        ((LocalTransform) localTransform).propagate();
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
     * Extends a {@link Transform} so that changes to properties are
     * automatically synchronized to the global transform. This might
     * in turn propagate to the global transform of all child graphics,
     * since they are affected by transform changes in the parent.
     */
    private class LocalTransform extends Transform {

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            propagate();
        }

        @Override
        public void setPosition(Point2D position) {
            super.setPosition(position);
            propagate();
        }

        @Override
        public void setRotation(Angle rotation) {
            super.setRotation(rotation);
            propagate();
        }

        @Override
        public void setScaleX(float scaleX) {
            super.setScaleX(scaleX);
            propagate();
        }

        @Override
        public void setScaleY(float scaleY) {
            super.setScaleY(scaleY);
            propagate();
        }

        @Override
        public void setFlipHorizontal(boolean flipHorizontal) {
            super.setFlipHorizontal(flipHorizontal);
            propagate();
        }

        @Override
        public void setFlipVertical(boolean flipVertical) {
            super.setFlipVertical(flipVertical);
            propagate();
        }

        @Override
        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
            propagate();
        }

        @Override
        public void setMaskColor(ColorRGB maskColor) {
            super.setMaskColor(maskColor);
            propagate();
        }

        private void propagate() {
            if (parent == null) {
                globalTransform.set(this);
            } else {
                Transform parentTransform = parent.getLocation().globalTransform;
                propagate(parentTransform);
            }

            for (Graphic2D child : children) {
                LocalTransform childTransform = (LocalTransform) child.getLocation().localTransform;
                childTransform.propagate();
            }
        }

        private void propagate(Transform parentTransform) {
            globalTransform.setVisible(visible && parentTransform.visible);
            globalTransform.setPosition(position.move(parentTransform.position));
            globalTransform.setRotation(rotation.degrees() + parentTransform.rotation.degrees());
            globalTransform.setScaleX(Aggregate.multiplyPercentage(scaleX, parentTransform.scaleX));
            globalTransform.setScaleY(Aggregate.multiplyPercentage(scaleY, parentTransform.scaleY));
            globalTransform.setFlipHorizontal(flipHorizontal || parentTransform.flipHorizontal);
            globalTransform.setFlipVertical(flipVertical || parentTransform.flipVertical);
            globalTransform.setAlpha(Aggregate.multiplyPercentage(alpha, parentTransform.alpha));
            globalTransform.setMaskColor(maskColor != null ? maskColor : parentTransform.maskColor);
        }
    }
}
