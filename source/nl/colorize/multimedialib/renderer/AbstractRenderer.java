//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.Renderable;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides a skeleton implementation of the {@link Renderer} interface, without
 * imposing any restrictions related to the actual rendering.
 * <p>
 * This implementation is not thread safe. It is assumed that the renderer is
 * always used from the same rendering thread.
 */
public abstract class AbstractRenderer implements Renderer {

    private Canvas canvas;
    private List<Updatable> updateCallbacks;
    private List<Renderable> renderCallbacks;

    public AbstractRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.updateCallbacks = new CopyOnWriteArrayList<>();
        this.renderCallbacks = new CopyOnWriteArrayList<>();
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    @Override
    public void addUpdateCallback(Updatable callback) {
        updateCallbacks.add(callback);
    }

    protected void notifyUpdateCallbacks(float deltaTime) {
        for (Updatable callback : updateCallbacks) {
            callback.update(deltaTime);
        }
    }

    @Override
    public void addRenderCallback(Renderable callback) {
        renderCallbacks.add(callback);
    }

    protected void notifyRenderCallbacks(GraphicsContext graphics) {
        for (Renderable callback : renderCallbacks) {
            callback.render(graphics);
        }
    }
}
