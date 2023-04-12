//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.RenderCapabilities;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.stage.Stage;
import nl.colorize.util.Platform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Default implementation of the {@link SceneContext} interface that is backed
 * by a {@link Renderer}.
 */
public class RenderContext implements SceneContext {

    private RenderCapabilities renderCapabilities;
    private Stage stage;
    private SceneGraph activeSceneGraph;
    private SceneGraph requestedSceneGraph;
    private List<Scene> globalScenes;
    private FrameStats frameStats;

    /**
     * Initializes this context using the specified renderer. This constructor
     * should be called by the renderer itself, during the animation loop.
     */
    public RenderContext(Renderer renderer) {
        this.renderCapabilities = renderer.getCapabilities();
        this.globalScenes = new ArrayList<>();
        this.frameStats = new FrameStats();

        GraphicsMode graphicsMode = renderCapabilities.graphicsMode();
        Canvas canvas = renderCapabilities.getCanvas();
        this.stage = new Stage(graphicsMode, canvas, frameStats);
    }

    @Override
    public void update(float deltaTime) {
        stage.update(deltaTime);

        if (requestedSceneGraph != null) {
            activateRequestedScene();
        }

        updateSceneGraph(activeSceneGraph, deltaTime);
        updateGlobalScenes(deltaTime);
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

    private void updateGlobalScenes(float deltaTime) {
        Iterator<Scene> iterator = globalScenes.iterator();

        while (iterator.hasNext()) {
            Scene globalScene = iterator.next();
            globalScene.update(this, deltaTime);

            if (globalScene.isCompleted()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void changeScene(Scene requestedScene) {
        requestedSceneGraph = new SceneGraph(requestedScene);
    }

    @Override
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

    @Override
    public void attach(Updatable subScene) {
        attach(Scene.wrap(subScene));
    }

    @Override
    public void attachGlobalScene(Scene globalScene) {
        globalScenes.add(globalScene);
        globalScene.start(this);
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public String getRendererName() {
        return renderCapabilities.graphics().getClass().getSimpleName()
            .replace("GraphicsContext", "")
            .replace("Graphics", "");
    }

    @Override
    public RenderCapabilities getRenderCapabilities() {
        return renderCapabilities;
    }

    @Override
    public FrameStats getFrameStats() {
        return frameStats;
    }

    @Override
    public boolean quit() {
        boolean success = false;
        if (Platform.isWindows() || Platform.isMac()) {
            success = true;
            System.exit(0);
        }
        return success;
    }

    @Override
    public List<String> getDebugInformation() {
        int targetFPS = getDisplayMode().framerate();

        List<String> info = new ArrayList<>();
        info.add("Renderer:  " + getRendererName());
        info.add("Canvas:  " + getCanvas());
        info.add("Framerate:  " + frameStats.getFramerate() + " / " + targetFPS);
        info.add("Update time:  " + frameStats.getFrameUpdateTime() + "ms");
        info.add("Render time:  " + frameStats.getFrameRenderTime() + "ms");
        info.add("# Sprites:  " + frameStats.getSpriteCount());
        info.add("# Primitives:  " + frameStats.getPrimitiveCount());
        info.add("# Text:  " + frameStats.getTextCount());
        return info;
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
