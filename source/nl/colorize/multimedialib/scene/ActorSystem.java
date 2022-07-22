//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Complex scenes are expected to split logic into multiple systems, one for
 * each functional area. Systems can be active for the entire life time of
 * the scene, for a certain time period, or until a certain condition is
 * reached.
 * <p>
 * This class would ideally be named just {@code System}, but has been given
 * this name to avoid confusion with {@code java.lang.System}.
 */
@FunctionalInterface
public interface ActorSystem {

    /**
     * Performs initialization logic for this system. This method is called when
     * the system is added to the scene.
     */
    default void init(SceneContext context) {
    }

    public void update(SceneContext context, float deltaTime);

    /**
     * When true, removes this system from the scene once the current frame
     * update has been completed. By default, systems are active for the
     * duration of the scene they are a part of. Overriding this method enables
     * the system to end prematurely.
     */
    default boolean isCompleted() {
        return false;
    }

    public static ActorSystem wrap(Updatable delegate) {
        return (context, deltaTime) -> delegate.update(deltaTime);
    }

    /**
     * Create a system that is only active for the specified duration in seconds,
     * after which it will be removed from the scene. The callback action is
     * performed every frame during this time.
     */
    public static ActorSystem timed(float duration, Runnable action) {
        Timer timer = new Timer(duration);

        return new ActorSystem() {
            @Override
            public void update(SceneContext context, float deltaTime) {
                timer.update(deltaTime);
                action.run();
            }

            @Override
            public boolean isCompleted() {
                return timer.isCompleted();
            }
        };
    }

    /**
     * Creates a system that will wait until the specified duration in seconds
     * has been reached, after which it will perform the specified action and
     * immediately end.
     */
    public static ActorSystem delay(float duration, Runnable action) {
        Timer timer = new Timer(duration);

        return new ActorSystem() {
            @Override
            public void update(SceneContext context, float deltaTime) {
                timer.update(deltaTime);
                if (timer.isCompleted()) {
                    action.run();
                }
            }

            @Override
            public boolean isCompleted() {
                return timer.isCompleted();
            }
        };
    }

    /**
     * Creates a system that operates based on the specified timeline. The callback
     * action is performed every frame based on the timeline's current value. If
     * the timeline loops, the system will remain active for the duration of the
     * scene. If the timeline does not loop, the system ends when the timeline ends.
     */
    public static ActorSystem fromTimeline(Timeline timeline, Consumer<Float> action) {
        return new ActorSystem() {
            @Override
            public void update(SceneContext context, float deltaTime) {
                timeline.movePlayhead(deltaTime);
                action.accept(timeline.getValue());
            }

            @Override
            public boolean isCompleted() {
                return timeline.isCompleted() && !timeline.isLoop();
            }
        };
    }

    /**
     * Creates a new system that excutes two other systems in sequence. First,
     * system A will be active until it has been completed. Then, system B will
     * be active until that has also be completed.
     */
    public static ActorSystem sequence(ActorSystem a, ActorSystem b) {
        List<ActorSystem> remaining = new ArrayList<>();
        remaining.add(a);
        remaining.add(b);

        return new ActorSystem() {
            @Override
            public void update(SceneContext context, float deltaTime) {
                ActorSystem current = remaining.get(0);
                current.update(context, deltaTime);
                if (current.isCompleted()) {
                    remaining.remove(0);
                }
            }

            @Override
            public boolean isCompleted() {
                return remaining.isEmpty();
            }
        };
    }
}
