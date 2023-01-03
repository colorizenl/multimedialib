//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

/**
 * Observer interface to receive notifications whenever graphics are added to
 * or removed from the stage.
 */
public interface StageObserver {

    public void onLayerAdded(Layer2D layer);

    public void onGraphicAdded(Layer2D layer, Graphic2D graphic);

    public void onGraphicRemoved(Layer2D layer, Graphic2D graphic);

    default void onModelAdded(PolygonModel model) {
    }

    default void onModelRemoved(PolygonModel model) {
    }

    public void onStageCleared();
}
