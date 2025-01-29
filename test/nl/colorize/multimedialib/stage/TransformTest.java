//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformTest {

    @Test
    void bulkSet() {
        Transform a = new Transform();
        a.setPosition(10, 20);

        Transform b = new Transform();
        b.setPosition(30, 40);

        a.set(b);

        assertEquals("(30, 40)", a.getPosition().toString());
        assertEquals("(30, 40)", b.getPosition().toString());
    }

    @Test
    void combine() {
        Transform a = new Transform();
        a.setPosition(20f, 0f);
        a.setRotation(90f);
        a.setScaleX(50f);

        Transform b = new Transform();
        b.setPosition(30f, 40f);
        b.setRotation(180f);
        b.setScaleX(150f);

        Transform combined = a.combine(b);

        assertEquals(50f, combined.getPosition().x(), EPSILON);
        assertEquals(40f, combined.getPosition().y(), EPSILON);
        assertEquals(270f, combined.getRotation().degrees(), EPSILON);
        assertEquals(75f, combined.getScaleX(), EPSILON);
    }
}
