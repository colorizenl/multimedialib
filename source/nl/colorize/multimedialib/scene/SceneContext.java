//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Provides an interface to add context information that can be accessed by
 * scenes.
 */
public interface SceneContext {

    public void changeScene(Scene requestedScene);

    public Canvas getCanvas();

    public InputDevice getInputDevice();

    public MediaLoader getMediaLoader();

    public ApplicationData getApplicationData(String appName);

    public float getAverageFPS();

    public float getAverageFrameTime();
}
