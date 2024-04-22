//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import org.junit.jupiter.api.Test;

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
}
