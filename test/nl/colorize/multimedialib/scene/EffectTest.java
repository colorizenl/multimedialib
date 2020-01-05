//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.graphics.Align;
import nl.colorize.multimedialib.graphics.AlphaTransform;
import nl.colorize.multimedialib.graphics.Sprite;
import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.mock.MockRenderer;
import nl.colorize.util.animation.Timeline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EffectTest {

    private static final float EPSILON = 0.001f;

    @Test
    public void testEffect() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 0f);
        timeline.addKeyFrame(1f, 10f);
        timeline.addKeyFrame(2f, 30f);

        Effect effect = Effect.forImage(new MockImage(), timeline);
        effect.modify(value -> effect.getPosition().setY(value));

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

    @Test
    public void testForSpriteX() {
        Sprite sprite = new Sprite();
        sprite.addState("mock", new MockImage());

        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 10f);
        timeline.addKeyFrame(2f, 20f);

        Effect effect = Effect.forSpriteX(sprite, timeline);

        assertEquals(0f, effect.getPosition().getX(), EPSILON);
        effect.update(0.00001f);
        assertEquals(10f, effect.getPosition().getX(), EPSILON);
        effect.update(1f);
        assertEquals(15f, effect.getPosition().getX(), EPSILON);
        effect.update(1f);
        assertEquals(20f, effect.getPosition().getX(), EPSILON);
    }

    @Test
    public void testForTextAlpha() {
        Timeline timeline = new Timeline();
        timeline.addKeyFrame(0f, 100f);
        timeline.addKeyFrame(2f, 0f);

        Effect effect = Effect.forTextAlpha("test", null, Align.LEFT, timeline);

        assertEquals(100f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(0.001f);
        assertEquals(100f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(1f);
        assertEquals(50f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(1f);
        assertEquals(0f, effect.getTransform().getAlpha(), EPSILON);
    }

    @Test
    public void testForTextAppear() {
        Effect effect = Effect.forTextAppear("test", null, Align.LEFT, 4f);

        List<String> rendered = new ArrayList<>();
        MockRenderer renderer = new MockRenderer() {
            @Override
            public void drawText(String text, TTFont font, float x, float y, Align al, AlphaTransform at) {
                rendered.add(text);
            }
        };

        assertEquals(100f, effect.getTransform().getAlpha(), EPSILON);
        effect.update(1f);
        effect.render(renderer);
        effect.update(1f);
        effect.render(renderer);
        effect.update(2f);
        effect.render(renderer);

        assertEquals(3, rendered.size());
        assertEquals("t", rendered.get(0));
        assertEquals("te", rendered.get(1));
        assertEquals("test", rendered.get(2));
    }
}
