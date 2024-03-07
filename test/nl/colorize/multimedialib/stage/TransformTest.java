//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
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

        child.combine(parent);

        assertEquals(40f, child.getPosition().x(), EPSILON);
        assertEquals(60f, child.getPosition().y(), EPSILON);
        assertTrue(child.isFlipHorizontal());
        assertFalse(child.isFlipVertical());
        assertEquals(100f, child.getRotation().degrees(), EPSILON);
        assertEquals(-75f, child.getScaleX(), EPSILON);
        assertEquals(100f, child.getScaleY(), EPSILON);
        assertEquals(80f, child.getAlpha(), EPSILON);
    }

    @Test
    void inheritVisibleField() {
        Transform first = new Transform();
        first.setVisible(true);

        Transform second = new Transform();
        second.setVisible(false);

        Transform third = new Transform();
        third.setVisible(true);

        second.combine(first);
        third.combine(second);

        assertTrue(first.isVisible());
        assertFalse(second.isVisible());
        assertFalse(third.isVisible());
    }
}
