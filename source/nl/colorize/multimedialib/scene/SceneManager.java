//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.RenderCallback;
import nl.colorize.multimedialib.renderer.RenderContext;
import nl.colorize.multimedialib.renderer.Renderer;

/**
 * Controls which scene is currently being played. Once the scene manager has
 * been registered as a callback with the renderer, it will take care of scene
 * management, meaning that it ensures that each scene is properly started,
 * stopped, updated, and changed.
 */
public class SceneManager implements RenderCallback {

    private Renderer renderer;
    private Scene currentScene;
    private Scene requestedScene;

    public SceneManager(Renderer renderer, Scene initialScene) {
        this.renderer = renderer;
        this.requestedScene = initialScene;
    }

    public void changeScene(Scene requestedScene) {
        this.requestedScene = requestedScene;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    @Override
    public void onFrame(float deltaTime, InputDevice input) {
        if (requestedScene != null) {
            if (currentScene != null) {
                currentScene.onSceneEnd();
            }

            currentScene = requestedScene;
            requestedScene = null;

            if (currentScene != null) {
                currentScene.onSceneStart(renderer.getMediaLoader());
            }
        }

        if (currentScene != null) {
            currentScene.onFrame(deltaTime, input);
        }
    }

    @Override
    public void onRender(RenderContext context) {
        if (currentScene != null) {
            currentScene.onRender(context);
        }
    }
}
