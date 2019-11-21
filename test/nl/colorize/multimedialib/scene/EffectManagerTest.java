//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.util.animation.Timeline;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class EffectManagerTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testPlayEffectUntilDone() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);
        timeline.addKeyFrame(2f, 30f);

        AtomicInteger count = new AtomicInteger();

        Effect effect = new Effect(new MockImage(), timeline, (e, v) -> {
            e.getPosition().setY(v);
            count.addAndGet(1);
        });

        EffectManager effectManager = new EffectManager();
        effectManager.play(effect);

        effectManager.update(1f);
        effectManager.update(1f);
        effectManager.update(1f);
        effectManager.update(1f);

        assertEquals(30f, effect.getPosition().getY(), EPSILON);
    }
}
