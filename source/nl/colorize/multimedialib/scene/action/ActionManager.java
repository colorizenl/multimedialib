//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.action;

import nl.colorize.multimedialib.renderer.Drawable;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;
import nl.colorize.multimedialib.renderer.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mechanism to manage and registration and playback of multiple {@link Action}s
 * and {@link Effect}s during a scene. The scene is responsible for updating and
 * drawing the {@code ActionManager} every frame.
 * <p>
 * Despite its name, this class is not only limited to playing actions. It can
 * also play effects and other types of objects that require frame updates.
 */
public class ActionManager implements Updatable, Drawable {

    private List<Action> actions;

    public ActionManager() {
        this.actions = new ArrayList<>();
    }

    public void play(Action action) {
        actions.add(action);
    }

    public void play(Effect effect) {
        actions.add(effect);
    }

    public void play(Timer timer) {
        actions.add(timer);
    }

    public void play(Updatable action) {
        Action wrapper = Action.indefinitely(action);
        actions.add(wrapper);
    }
    
    public void cancel(Action action) {
        actions.remove(action);
    }

    public void cancelAll() {
        actions.clear();
    }

    @Override
    public void update(float deltaTime) {
        Action[] buffer = actions.toArray(new Action[0]);

        for (Action action : buffer) {
            action.update(deltaTime);
            if (action.isCompleted()) {
                actions.remove(action);
            }
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        for (Action action : actions) {
            if (action instanceof Drawable) {
                Drawable graphic = (Drawable) action;
                graphic.render(graphics);
            }
        }
    }
}
