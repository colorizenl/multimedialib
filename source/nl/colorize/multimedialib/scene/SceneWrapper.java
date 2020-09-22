//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.renderer.Drawable;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.Updatable;

import java.util.List;

/**
 * Wraps an update and/or render callback into a {@link Scene}.
 */
public class SceneWrapper implements Scene {

    private Updatable updateCallback;
    private List<Drawable> graphics;

    private SceneWrapper(Updatable updateCallback, Drawable... graphics) {
        this.updateCallback = updateCallback;
        this.graphics = ImmutableList.copyOf(graphics);
    }

    @Override
    public void start(Application app) {
    }

    @Override
    public void update(Application app, float deltaTime) {
        if (updateCallback != null) {
            updateCallback.update(deltaTime);
        }
    }

    @Override
    public void render(Application app, GraphicsContext2D graphicsContext) {
        for (Drawable graphic : graphics) {
            graphic.render(graphicsContext);
        }
    }

    public static Scene wrap(Updatable updateCallback, Drawable... graphics) {
        return new SceneWrapper(updateCallback, graphics);
    }
}
