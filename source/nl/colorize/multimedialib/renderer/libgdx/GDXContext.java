//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import lombok.Getter;
import nl.colorize.multimedialib.renderer.FrameStats;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.multimedialib.scene.SceneManager;

/**
 * libGDX application that acts as an implementation of {@link SceneContext}
 * for the libGDX renderer.
 * <p>
 * This class does not configure or define any particular libGDX back-end.
 * Subclasses are responsible for configuring and launching the application.
 */
@Getter
public abstract class GDXContext implements ApplicationListener, SceneContext {

    protected RenderConfig config;
    protected SceneManager sceneManager;

    protected GDXGraphics graphics;
    protected InputDevice input;
    protected GDXMediaLoader mediaLoader;
    protected Network network;

    @Override
    public final void create() {
        initContext();
        if (config.getGraphicsMode() == GraphicsMode.MODE_3D) {
            sceneManager.getStage().setWorld3D(graphics);
        }
        resize(config.getCanvas().getWidth(), config.getCanvas().getHeight());
    }

    protected abstract void initContext();

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        // Hard quit because libGDX otherwise takes several
        // seconds to close the application.
        terminate();
    }

    @Override
    public final void resize(int width, int height) {
        config.getCanvas().resizeScreen(width, height);
        HdpiUtils.glViewport(0, 0, width, height);
        graphics.restartBatch();
    }

    @Override
    public final void render() {
        sceneManager.requestFrameUpdate();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        getFrameStats().markStart(FrameStats.PHASE_FRAME_RENDER);
        getStage().visit(graphics);
        getFrameStats().markEnd(FrameStats.PHASE_FRAME_RENDER);

        prepareFrame();
    }

    protected abstract void prepareFrame();
}
