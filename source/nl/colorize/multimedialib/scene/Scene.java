//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

/**
 * Represents a discrete part or phase of an application, that is active for
 * some period of time. Simple applications might consist of a single scene,
 * complex applications can have dozens.
 * <p>
 * Scenes will receive updates at various points throughout their life cycle:
 * when a scene starts, either because the scene is explictly started or
 * because it's the application's initial scene; during frame updates, and
 * when rendering.
 */
public interface Scene extends Updatable, Renderable {

    public void start(SceneContext context);
}
