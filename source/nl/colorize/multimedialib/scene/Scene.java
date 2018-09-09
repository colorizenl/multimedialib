//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RenderContext;

/**
 * Represents a discrete part or phase of an application, that is active for
 * some period of time. Simple applications might consist of a single scene,
 * complex applications can have dozens.
 * <p>
 * Scenes will receive updates at various points throughout their life cycle:
 * when a scene starts (either because the scene is explictly started or
 * because it's the application's initial scene), when the scene ends (because
 * the application switches to another scene or because the application is
 * terminated), and during frame updates. These frame updates are split into
 * an update phase and a rendering phase. The renderer will attempt to call
 * both as close to the targeted framerate as possible, but this may not
 * always be possible depending on performance. In such cases, the renderer
 * can prioritize frame updates over frame renders. In other words, one update
 * is not always followed by one render, in some cases there could be several
 * updates before the render is performed.
 */
public interface Scene {

    public void onSceneStart(MediaLoader mediaLoader);

    public void onFrame(float deltaTime, InputDevice input);
    
    public void onRender(RenderContext context);
    
    default void onSceneEnd() {
    }
}
