//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.action;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.util.animation.Timeline;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionManagerTest {

    private static final float EPSILON = 0.001f;

    @Test
    void playEffectUntilDone() {
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

        ActionManager manager = new ActionManager();
        manager.play(effect);

        manager.update(1f);
        manager.update(1f);
        manager.update(1f);
        manager.update(1f);

        assertEquals(30f, effect.getPosition().getY(), EPSILON);
    }

    @Test
    void playTimer() {
        List<String> called = new ArrayList<>();
        Timer timer = new Timer(2f);
        timer.attach(() -> called.add("1"));

        ActionManager manager = new ActionManager();
        manager.play(timer);
        manager.update(1f);
        manager.update(1f);
        manager.update(1f);

        assertEquals(1, called.size());
        assertEquals(2f, timer.getTime(), EPSILON);
        assertTrue(timer.isCompleted());
    }
}
