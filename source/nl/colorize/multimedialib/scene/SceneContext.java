//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides access to the contents of the currently active scene. This includes
 * both the stage and platform resources via the renderer.
 * <p>
 * This class also allows <em>sub-scenes</em> to be attached to the current
 * scene. These sub-scenes can contain their own logic, but cannot outlive their
 * parent scene. When the active scene is changed, both the scene itself and its
 * sub-scenes will be terminated and the stage will be cleared in preparation
 * for the next scene.
 */
public class SceneContext implements Updatable {

    private DisplayMode displayMode;
    private InputDevice input;
    private MediaLoader mediaLoader;
    private Network network;

    private Stage stage;
    private SceneGraph activeSceneGraph;
    private SceneGraph requestedSceneGraph;
    private FrameStats frameStats;

    /**
     * Initializes a new {@link SceneContext} for the specified renderer. This
     * constructor is called by the renderer, there is no need to call it
     * directly from application code.
     */
    public SceneContext(Renderer renderer, Scene initialScene) {
        this.displayMode = renderer.getDisplayMode();
        this.input = renderer.accessInputDevice();
        this.mediaLoader = renderer.accessMediaLoader();
        this.network = renderer.accessNetwork();

        this.stage = new Stage(renderer.getGraphicsMode(), displayMode.canvas());
        this.frameStats = new FrameStats(displayMode.framerate());

        changeScene(initialScene);
    }

    /**
     * Updates application logic for the current scene, then renders the
     * contents of the stage. This method is called by the renderer as part
     * of every frame update.
     */
    @Override
    public void update(float deltaTime) {
        stage.update(deltaTime);

        if (requestedSceneGraph != null) {
            activateRequestedScene();
        }

        updateSceneGraph(activeSceneGraph, deltaTime);
    }

    private void updateSceneGraph(SceneGraph current, float deltaTime) {
        current.scene.update(this, deltaTime);

        // Iterate the list of systems backwards to handle
        // concurrent modification while the list is being
        // iterated, without having to create a copy of the
        // list every frame.
        for (int i = current.subScenes.size() - 1; i >= 0; i--) {
            SceneGraph subScene = current.subScenes.get(i);

            // We need to check twice if the sub-scene has
            // been completed, both before and after its
            // own update.
            if (!checkCompleted(current, subScene)) {
                updateSceneGraph(subScene, deltaTime);
                checkCompleted(current, subScene);
            }
        }
    }

    private boolean checkCompleted(SceneGraph parent, SceneGraph subScene) {
        if (subScene.scene.isCompleted()) {
            subScene.scene.end(this);
            parent.subScenes.remove(subScene);
            return true;
        } else {
            return false;
        }
    }

    private void activateRequestedScene() {
        if (activeSceneGraph != null) {
            activeSceneGraph.walk(scene -> scene.end(this));
            stage.clear();
        }

        activeSceneGraph = requestedSceneGraph;
        activeSceneGraph.walk(scene -> scene.start(this));
        requestedSceneGraph = null;
    }

    /**
     * Requests to change the active scene after the current frame update has
     * been completed. If another scene had already been requested, calling
     * this method again will overrule that request.
     */
    public void changeScene(Scene requestedScene) {
        requestedSceneGraph = new SceneGraph(requestedScene);
    }

    /**
     * Attaches a sub-scene to the currently active scene. The sub-scene will
     * remain active until it is detached or the parent scene ends.
     */
    public void attach(Scene subScene) {
        SceneGraph subSceneGraph = new SceneGraph(subScene);

        // We iterate the list of sub-scenes backwards to allow for
        // concurrent modification, but that means we need to store
        // the list in reverse order to keep the expected behavior.
        // This is a relatively expensive operation, but we expect
        // iterating sub-scenes is done *much* more often than adding
        // sub-scenes.
        if (requestedSceneGraph == null) {
            activeSceneGraph.subScenes.add(0, subSceneGraph);
            subScene.start(this);
        } else {
            requestedSceneGraph.subScenes.add(0, subSceneGraph);
        }
    }

    /**
     * Convenience method that converts an {@link Updatable} to a sub-scene,
     * then attaches it to the currently active scene. The sub-scene will follow
     * the same life-cycle as described in {@link #attach(Scene)}.
     */
    public void attach(Updatable subScene) {
        attach(Scene.wrap(subScene));
    }

    public Stage getStage() {
        return stage;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public Canvas getCanvas() {
        return stage.getCanvas();
    }

    public InputDevice getInputDevice() {
        return input;
    }

    public MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    public Network getNetwork() {
        return network;
    }

    public FrameStats getFrameStats() {
        return frameStats;
    }

    /**
     * Attempts to quit the application. Returns false if the current platform
     * does not allow the application to quit.
     */
    public boolean quit() {
        boolean success = false;
        if (Platform.isWindows() || Platform.isMac()) {
            success = true;
            System.exit(0);
        }
        return success;
    }

    /**
     * The currently active scene consists of the parent scene, plus an optional
     * number of sub-scenes. The sub-scenes can contain their own logic, but
     * cannot live their parent scene. When walking the scene graph, the parent
     * scene is visited before its sub-scenes.
     */
    private record SceneGraph(Scene scene, List<SceneGraph> subScenes) {

        public SceneGraph(Scene scene) {
            this(scene, new ArrayList<>());
        }

        public void walk(Consumer<Scene> callback) {
            callback.accept(scene);
            subScenes.forEach(subScene -> subScene.walk(callback));
        }
    }
}
