//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.MessageQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * Sub-scene that can be used to track swipe gestures for both mouse pointers
 * and touch controls. Instances need to be attached to the current scene in
 * order to receive frame updates.
 * <p>
 * Swipe gestures are detected using a tolerance. This is necessary to prevent
 * mis-identifying clicks and taps as swipe gestures.
 */
public class SwipeTracker implements Scene {

    private float tolerance;
    private Map<String, Point2D> incompleteSwipes;
    @Getter private MessageQueue<Line> swipes;

    public SwipeTracker(float tolerance) {
        Preconditions.checkArgument(tolerance >= 10f, "Invalid tolerance: " + tolerance);

        this.tolerance = tolerance;
        this.incompleteSwipes = new HashMap<>();
        this.swipes = new MessageQueue<>();
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        for (Pointer pointer : context.getInput().getPointers()) {
            if (incompleteSwipes.containsKey(pointer.getId())) {
                updateCurrentSwipe(pointer);
            } else {
                checkSwipeStart(pointer);
            }
        }
    }

    private void checkSwipeStart(Pointer pointer) {
        if (pointer.isPressed()) {
            incompleteSwipes.put(pointer.getId(), pointer.getPosition());
        }
    }

    private void updateCurrentSwipe(Pointer pointer) {
        if (pointer.isReleased()) {
            Point2D start = incompleteSwipes.get(pointer.getId());
            Point2D position = pointer.getPosition();

            if (start.distanceTo(position) >= tolerance) {
                Line completedSwipe = new Line(start, position);
                swipes.offer(completedSwipe);
            }

            incompleteSwipes.remove(pointer.getId());
        } else if (!pointer.isPressed()) {
            incompleteSwipes.remove(pointer.getId());
        }
    }
}
