//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Callback interface for objects that will be notified during every frame
 * update and every frame render, for as long as the renderer is active.
 */
public interface RenderCallback {

    public void onFrame(float deltaTime, InputDevice input);

    public void onRender(RenderContext context);
}
