//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Buffer;
import nl.colorize.multimedialib.math.Line;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.InputDevice;

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
    private Point2D currentSwipeStart;
    @Getter private Buffer<Line> swipes;

    public SwipeTracker(float tolerance) {
        Preconditions.checkArgument(tolerance >= 10f, "Invalid tolerance: " + tolerance);

        this.tolerance = tolerance;
        this.swipes = new Buffer<>();
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        InputDevice input = context.getInput();
        Point2D pointer = input.getPointer().orElse(null);

        if (pointer == null) {
            currentSwipeStart = null;
        } else if (currentSwipeStart == null) {
            checkSwipeStart(input, pointer);
        } else {
            updateCurrentSwipe(input, pointer);
        }
    }

    private void checkSwipeStart(InputDevice input, Point2D pointer) {
        if (input.isPointerPressed()) {
            currentSwipeStart = pointer;
        }
    }

    private void updateCurrentSwipe(InputDevice input, Point2D pointer) {
        if (input.isPointerReleased()) {
            if (currentSwipeStart.distanceTo(pointer) >= tolerance) {
                Line completedSwipe = new Line(currentSwipeStart, pointer);
                swipes.push(completedSwipe);
            }

            currentSwipeStart = null;
        } else if (!input.isPointerPressed()) {
            currentSwipeStart = null;
        }
    }
}
