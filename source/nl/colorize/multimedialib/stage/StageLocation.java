//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.UUID;

/**
 * Describes a graphic's location within the scene graph that is currently
 * being displayed on the stage. This class exists to avoid repeated logic
 * between different types of graphics.
 */
public class StageLocation {

    @Getter private UUID id;
    @Getter private Container parent;
    @Getter private Transform localTransform;

    public StageLocation() {
        this.id = UUID.randomUUID();
        this.parent = null;
        this.localTransform = new Transform();
    }

    public void attach(Container parent) {
        Preconditions.checkState(this.parent == null, "Graphic is already attached to parent");
        this.parent = parent;
    }

    public void detach() {
        parent = null;
    }

    public boolean isAttached() {
        return parent != null;
    }

    public Transform getGlobalTransform() {
        if (parent == null) {
            return localTransform;
        }
        return localTransform.combine(parent.getGlobalTransform());
    }
}
