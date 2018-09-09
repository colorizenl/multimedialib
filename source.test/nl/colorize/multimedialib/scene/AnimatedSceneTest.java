//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockAnimatable;
import nl.colorize.multimedialib.renderer.RenderContext;
import nl.colorize.util.animation.Timeline;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnimatedSceneTest {

    @Test
    public void testUpdateRegisteredAnimations() {
        MockAnimatable first = new MockAnimatable();
        MockAnimatable second = new MockAnimatable();

        MockAnimatedScene scene = new MockAnimatedScene();
        scene.add(first);
        scene.onSceneStart(null);
        scene.onFrame(0.1f, null);
        scene.add(second);
        scene.onFrame(0.1f, null);

        assertEquals(2, first.getFrameCount());
        assertEquals(1, second.getFrameCount());
    }

    @Test
    public void testTemporaryAnimation() {
        MockAnimatedScene scene = new MockAnimatedScene();
        MockAnimatable anim = new MockAnimatable();
        scene.add(anim, 0.2f);
        scene.onSceneStart(null);
        scene.onFrame(0.1f, null);
        scene.onFrame(0.1f, null);
        scene.onFrame(0.1f, null);

        assertEquals(2, anim.getFrameCount());
    }

    @Test
    public void testTimelineAnimation() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);

        MockAnimatedScene scene = new MockAnimatedScene();
        scene.add(timeline);
        scene.onSceneStart(null);
        scene.onFrame(0.1f, null);
        scene.onFrame(0.1f, null);

        assertEquals(0.2f, timeline.getPlayhead(), 0.001f);

        scene.onFrame(1f, null);

        assertEquals(1, timeline.getPlayhead(), 0.001f);
        assertEquals(0, scene.getContents().size());
    }

    @Test
    public void testDoNotAnimateWhenSceneIsNotActive() {
        MockAnimatedScene scene = new MockAnimatedScene();
        MockAnimatable anim = new MockAnimatable();
        scene.add(anim);
        scene.onSceneStart(null);
        scene.onFrame(0.1f, null);
        scene.onFrame(0.1f, null);
        scene.onSceneEnd();
        scene.onFrame(0.1f, null);

        assertEquals(2, anim.getFrameCount());
    }

    private static class MockAnimatedScene extends AnimatedScene {

        @Override
        public void onRender(RenderContext context) {
            // Do nothing
        }
    }
}