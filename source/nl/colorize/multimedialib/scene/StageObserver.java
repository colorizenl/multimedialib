//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.Graphic2D;

/**
 * Observer interface to receive notifications whenever graphics are added to
 * or removed from the stage.
 */
public interface StageObserver {

    public void onGraphicAdded(Layer layer, Graphic2D graphic);

    public void onGraphicRemoved(Layer layer, Graphic2D graphic);

    public void onStageCleared();
}
