//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SizeTest {

    @Test
    void cannotCreateNegativeSize() {
        new Size(1, 1);
        assertThrows(IllegalArgumentException.class, () -> new Size(-1, 1));
    }

    @Test
    void multiply() {
        assertEquals("200x100", new Size(200, 100).multiply(1f).toString());
        assertEquals("400x200", new Size(200, 100).multiply(2f).toString());
    }
}
