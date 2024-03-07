//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.collect.Iterables;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.scene.effect.SwipeTracker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwipeTrackerTest {

    @Test
    void noSwipeWithoutPressingPointer() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SwipeTracker swipeTracker = new SwipeTracker(10f);

        renderer.setPointer(new Point2D(10f, 20f));
        swipeTracker.update(renderer.getContext(), 1f);

        renderer.setPointer(new Point2D(30f, 40f));
        swipeTracker.update(renderer.getContext(), 1f);

        assertEquals("[]", Iterables.toString(swipeTracker.getSwipes().flush()));
    }

    @Test
    void noSwipeWithoutReleasingPointer() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SwipeTracker swipeTracker = new SwipeTracker(10f);

        renderer.setPointerPressed(true);
        renderer.setPointer(new Point2D(10f, 20f));
        swipeTracker.update(renderer.getContext(), 1f);

        renderer.setPointer(new Point2D(30f, 40f));
        swipeTracker.update(renderer.getContext(), 1f);

        assertEquals("[]", Iterables.toString(swipeTracker.getSwipes().flush()));
    }

    @Test
    void detectSwipes() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SwipeTracker swipeTracker = new SwipeTracker(10f);

        renderer.setPointerPressed(true);
        renderer.setPointer(new Point2D(10f, 20f));
        swipeTracker.update(renderer.getContext(), 1f);

        renderer.setPointerReleased(true);
        renderer.setPointer(new Point2D(30f, 40f));
        swipeTracker.update(renderer.getContext(), 1f);

        assertEquals("[(10, 20) -> (30, 40)]", Iterables.toString(swipeTracker.getSwipes().flush()));
    }

    @Test
    void swipeBelowToleranceDoesNotCount() {
        HeadlessRenderer renderer = new HeadlessRenderer();
        SwipeTracker swipeTracker = new SwipeTracker(10f);

        renderer.setPointerPressed(true);
        renderer.setPointer(new Point2D(10f, 20f));
        swipeTracker.update(renderer.getContext(), 1f);

        renderer.setPointerReleased(true);
        renderer.setPointer(new Point2D(11f, 21));
        swipeTracker.update(renderer.getContext(), 1f);

        assertEquals("[]", Iterables.toString(swipeTracker.getSwipes().flush()));
    }
}
