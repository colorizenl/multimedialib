//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.action;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.Updatable;

/**
 * Logic that is active for a certain amount of time during a scene. Actions
 * can be used to declare scene behavior in a declarative way. Behavior can
 * be added when the action is started, when it completes, for every frame
 * while it is active. Actions can be configured to remain active for a certain
 * amount of time, until a certain condition is reached, or indefinitely for
 * the remainder of the scene.
 * <p>
 * The administration of playing actions is usually handled in combination
 * with a {@link ActionManager} that keeps track of all actions that are played
 * during a scene.
 * <p>
 * Actions contain only logic and do not define graphical behavior. Use
 * {@link Effect}s to have a similar mechanism for defining graphics that
 * should be shown during the scene.
 * <p>
 * In addition to defining the action interface itself, this interface also
 * contains a number of static factory and utility methods for when working
 * with actions.
 */
public interface Action extends Updatable {

    /**
     * Contains the action's logic. This is called every frame while the action
     * is active, i.e. until {@link #isCompleted()} starts returning true.
     */
    @Override
    public void update(float deltaTime);

    /**
     * Indicates when this action has been completed. Actions initially start
     * as active, and will then receive frame updates. After every frame update
     * this method is called to determine the action's status.
     */
    public boolean isCompleted();

    /**
     * Creates an action that is completed immediately. In other words, this will
     * create an action that is only active for a single frame.
     */
    public static Action once(Updatable callback) {
        return new Action() {
            boolean completed = false;

            @Override
            public void update(float deltaTime) {
                if (!completed) {
                    callback.update(deltaTime);
                    completed = true;
                }
            }

            @Override
            public boolean isCompleted() {
                return completed;
            }
        };
    }

    /**
     * Creates an action that is completed immediately. In other words, this will
     * create an action that is only active for a single frame.
     */
    public static Action once(Runnable callback) {
        return once(deltaTime -> callback.run());
    }

    /**
     * Creates an action that will remain active for a limited period of time,
     * as indicated by the specified duration in seconds.
     */
    public static Action timed(Updatable callback, float duration) {
        Preconditions.checkArgument(duration > 0f, "Invalid duration: " + duration);

        return new Action() {
            float time = 0f;
            boolean completed = false;

            @Override
            public void update(float deltaTime) {
                if (!completed) {
                    time += deltaTime;
                    callback.update(deltaTime);
                    completed = time >= duration;
                }
            }

            @Override
            public boolean isCompleted() {
                return completed;
            }
        };
    }

    /**
     * Creates an action that will remain active for a limited period of time,
     * as indicated by the specified duration in seconds.
     */
    public static Action timed(Runnable callback, float duration) {
        return timed(deltaTime -> callback.run(), duration);
    }

    /**
     * Creates an action that will remain active indefinitely.
     */
    public static Action indefinitely(Updatable callback) {
        return new Action() {
            @Override
            public void update(float deltaTime) {
                callback.update(deltaTime);
            }

            @Override
            public boolean isCompleted() {
                return false;
            }
        };
    }

    /**
     * Creates an action that will remain active indefinitely.
     */
    public static Action indefinitely(Runnable callback) {
        return indefinitely(deltaTime -> callback.run());
    }

    /**
     * Chains another action that should be performed immediately after this
     * action has been completed. This method returns a new {@code Action} with
     * the specified behavior.
     */
    public static Action chain(Action first, Action second) {
        return new Action() {
            @Override
            public void update(float deltaTime) {
                if (!first.isCompleted()) {
                    first.update(deltaTime);
                } else if (!second.isCompleted()) {
                    second.update(deltaTime);
                }
            }

            @Override
            public boolean isCompleted() {
                return first.isCompleted() && second.isCompleted();
            }
        };
    }

    /**
     * Prepends a waiting period to this action, during which nothing will happen,
     * and afterwards this action is executed as normally. This method returns a
     * new {@code Action} with the specified behavior.
     */
    public static Action delay(Action action, float duration) {
        Updatable placeholder = deltaTime -> {};
        return chain(timed(placeholder, duration), action);
    }
}
