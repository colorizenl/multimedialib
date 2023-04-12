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
import nl.colorize.multimedialib.renderer.RenderCapabilities;
import nl.colorize.multimedialib.stage.Stage;

import java.util.List;

/**
 * Provides access to the contents of the currently active scene. This includes
 * both the stage and platform resources via the renderer. The context is
 * passed to the scene during each frame update, and enables the scene to
 * handle said frame update.
 * <p>
 * This interface also allows <em>sub-scenes</em> to be attached to the current
 * scene. These sub-scenes can contain their own logic, but cannot outlive their
 * parent scene. When the active scene is changed, both the scene itself and its
 * sub-scenes will be terminated and the stage will be cleared in preparation
 * for the next scene.
 */
public interface SceneContext extends Updatable {

    /**
     * Updates application logic for the current scene, then renders the
     * contents of the stage. This method is called by the renderer as part
     * of every frame update.
     */
    @Override
    public void update(float deltaTime);

    /**
     * Requests to change the active scene after the current frame update has
     * been completed. If another scene had already been requested, calling
     * this method again will overrule that request.
     */
    public void changeScene(Scene requestedScene);

    /**
     * Attaches a sub-scene to the currently active scene. The sub-scene will
     * remain active until it is detached or the parent scene ends.
     */
    public void attach(Scene subScene);

    /**
     * Convenience method that converts an {@link Updatable} to a sub-scene,
     * then attaches it to the currently active scene. The sub-scene will follow
     * the same life-cycle as described in {@link #attach(Scene)}.
     */
    public void attach(Updatable subScene);

    /**
     * Attaches a scene that is *not* tied to the currently active scene, and
     * will remain active for the rest of the application. Multiple global
     * scenes can be attached.
     */
    public void attachGlobalScene(Scene globalScene);

    public Stage getStage();

    /**
     * Returns the display name for the renderer that is powering this context.
     * The display name does not include the word "renderer" itself.
     */
    public String getRendererName();

    public RenderCapabilities getRenderCapabilities();

    default DisplayMode getDisplayMode() {
        return getRenderCapabilities().displayMode();
    }

    default Canvas getCanvas() {
        return getRenderCapabilities().getCanvas();
    }

    default InputDevice getInputDevice() {
        return getRenderCapabilities().inputDevice();
    }

    default MediaLoader getMediaLoader() {
        return getRenderCapabilities().mediaLoader();
    }

    default Network getNetwork() {
        return getRenderCapabilities().network();
    }

    public FrameStats getFrameStats();

    /**
     * Attempts to quit the application. Returns false if the current platform
     * does not allow the application to quit.
     */
    public boolean quit();

    /**
     * Returns debug and support information that can be displayed when running
     * a MultimediaLib application in debug mode. The returned list is intended
     * to be displayed in a {@code Text}, which can be styled to match the
     * application appearance.
     */
    public List<String> getDebugInformation();
}
