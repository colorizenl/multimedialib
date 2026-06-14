//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RNGTest {

    @BeforeEach
    public void before() {
        RNG.seed(1234L);
    }

    @AfterEach
    public void after() {
        RNG.randomSeed();
    }

    @Test
    void generateDeterministicRandomNumbers() {
        assertEquals(28, RNG.getInt(0, 100));
        assertEquals(33, RNG.getInt(0, 100));
        assertEquals(33, RNG.getInt(0, 100));
        assertEquals(20, RNG.getInt(0, 100));
    }

    @Test
    void pickRandomListElement() {
        List<String> items = List.of("1", "2", "3", "4");

        assertEquals("3", RNG.pick(items));
        assertEquals("2", RNG.pick(items));
    }

    @Test
    void shuffleList() {
        List<String> items = List.of("1", "2", "3", "4");
        List<String> shuffled = RNG.shuffle(items);

        assertEquals("[1, 2, 3, 4]", items.toString());
        assertEquals("[1, 2, 4, 3]", shuffled.toString());
    }
}