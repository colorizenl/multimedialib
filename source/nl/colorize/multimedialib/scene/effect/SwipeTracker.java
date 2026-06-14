//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.scene.Actor;
import nl.colorize.util.SubscribableCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Actor that can be used to track swipe gestures for both mouse pointers and
 * touch controls. Instances need to be attached to the current scene in order
 * to receive frame updates.
 * <p>
 * Swipe gestures are detected using a tolerance. This is necessary to prevent
 * mis-identifying clicks and taps as swipe gestures.
 */
public class SwipeTracker implements Actor {

    private InputDevice input;
    private double tolerance;
    private Map<String, Point2D> incompleteSwipes;
    @Getter private SubscribableCollection<Line> swipes;

    public SwipeTracker(InputDevice input, double tolerance) {
        Preconditions.checkArgument(tolerance >= 10f, "Invalid tolerance: " + tolerance);

        this.input = input;
        this.tolerance = tolerance;
        this.incompleteSwipes = new HashMap<>();
        this.swipes = SubscribableCollection.wrap(new ArrayList<>());
    }

    @Override
    public void update(double deltaTime) {
        for (Pointer pointer : input.getPointers()) {
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
                swipes.add(completedSwipe);
            }

            incompleteSwipes.remove(pointer.getId());
        } else if (!pointer.isPressed()) {
            incompleteSwipes.remove(pointer.getId());
        }
    }
}
