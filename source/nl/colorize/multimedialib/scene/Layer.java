//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;

/**
 * Pointer for one of the 2D graphics layers on the stage. Any number of layers
 * can be added to the stage. The stage starts out with a default layer, which
 * is accessible via {@link #DEFAULT}. Graphics will be added to the default
 * layer if no layer is explicitly specified.
 */
public final class Layer {

    private String name;

    public static final Layer DEFAULT = new Layer("$$default");

    protected Layer(String name) {
        Preconditions.checkArgument(name.length() >= 1, "Empty layer name");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
