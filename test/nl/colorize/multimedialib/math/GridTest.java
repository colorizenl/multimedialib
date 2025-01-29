//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    @Test
    void resizeGrid() {
        Grid<String> grid = new Grid<>();
        grid.set(0, 0, "A");
        grid.set(1, 0, "B");
        grid.set(1, 1, "C");

        assertEquals("A", grid.get(0, 0));
        assertEquals("B", grid.get(1, 0));
        assertEquals("C", grid.get(1, 1));
        assertNull(grid.get(0, 1));
        assertNull(grid.get(2, 2));
    }

    @Test
    void startAtCoordinates() {
        Grid<String> grid = new Grid<>();
        grid.set(2, 1, "A");
        grid.set(3, 1, "B");

        assertEquals("A", grid.get(2, 1));
        assertEquals("B", grid.get(3, 1));
    }

    @Test
    void startAtNegativeCoordinates() {
        Grid<String> grid = new Grid<>();
        grid.set(0, 0, "A");
        grid.set(-1, 0, "B");
        grid.set(1, 0, "C");

        assertEquals("A", grid.get(0, 0));
        assertEquals("B", grid.get(-1, 0));
        assertEquals("C", grid.get(1, 0));
    }

    @Test
    void streamGrid() {
        Grid<String> grid = new Grid<>();
        grid.set(0, 0, "A");
        grid.set(1, 0, "B");
        grid.set(1, 1, "C");

        List<String> result = grid.stream().toList();

        assertEquals("[A, B, C]", result.toString());
    }

    @Test
    void streamRegion() {
        Grid<String> grid = new Grid<>();
        grid.set(0, 0, "A");
        grid.set(1, 0, "B");
        grid.set(2, 0, "C");
        grid.set(0, 1, "D");
        grid.set(1, 1, "E");
        grid.set(2, 1, "F");

        List<String> result = grid.stream(1, 0, 3, 2).toList();

        assertEquals("[B, C, E, F]", result.toString());
    }
}
