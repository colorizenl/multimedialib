//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransformTest {

    private static final float EPSILON = 0.1f;

    @Test
    void combineTransform() {
        Transform parent = new Transform();
        parent.setPosition(10f, 20f);
        parent.setFlipHorizontal(true);
        parent.setScaleX(50f);
        parent.setAlpha(80f);

        Transform child = new Transform();
        child.setRotation(100f);
        child.setPosition(30f, 40f);
        child.setScaleX(150f);

        Transform global = child.combine(parent);

        assertEquals(40f, global.getPosition().getX(), EPSILON);
        assertEquals(60f, global.getPosition().getY(), EPSILON);
        assertTrue(global.isFlipHorizontal());
        assertFalse(global.isFlipVertical());
        assertEquals(100f, global.getRotation(), EPSILON);
        assertEquals(-75f, global.getScaleX(), EPSILON);
        assertEquals(100f, global.getScaleY(), EPSILON);
        assertEquals(80f, global.getAlpha(), EPSILON);
    }
}
