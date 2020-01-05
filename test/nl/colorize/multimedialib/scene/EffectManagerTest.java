//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
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

        Effect effect = Effect.forImage(new MockImage(), timeline);
        effect.modify(value -> {
            effect.getPosition().setY(value);
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
