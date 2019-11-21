//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.util.animation.Timeline;
import org.junit.Test;

import static org.junit.Assert.*;

public class EffectTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testEffect() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);
        timeline.addKeyFrame(2f, 30f);

        Effect effect = new Effect(new MockImage(), timeline, (e, v) -> e.getPosition().setY(v));

        effect.update(0.5f);
        assertEquals(5f, effect.getPosition().getY(), EPSILON);
        effect.update(0.5f);
        assertEquals(10f, effect.getPosition().getY(), EPSILON);
        effect.update(0.5f);
        assertEquals(20f, effect.getPosition().getY(), EPSILON);
        effect.update(0.5f);
        assertEquals(30f, effect.getPosition().getY(), EPSILON);
        effect.update(0.5f);
        assertEquals(30f, effect.getPosition().getY(), EPSILON);
    }
}
