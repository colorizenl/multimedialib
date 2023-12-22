//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Two-dimensional data structure with linear access time to each cell. The
 * grid is dynamically resized when adding new cells, meaning it is relatively
 * slow to mutate the grid but fast to access it. Each cell is represented by
 * X and Y coordinates. The grid does not necessarily have to start at (0, 0),
 * it can start at any coordinate, including negative coordinates. Coordinates
 * can contain {@code null}, so not every cell needs to be occupied.
 *
 * @param <E> The type element which acts as a cell within this grid.
 */
public class Grid<E> {

    private int gridX0;
    private int gridY0;
    private int gridX1;
    private int gridY1;
    private E[][] cells;

    public Grid() {
        this(0, 0, 0, 0);
    }

    @SuppressWarnings("unchecked")
    private Grid(int x0, int y0, int x1, int y1) {
        validateCoordinates(x0, y0, x1, y1);

        this.gridX0 = x0;
        this.gridY0 = y0;
        this.gridX1 = x1;
        this.gridY1 = y1;
        this.cells = (E[][]) new Object[x1 - x0][y1 - y0];
    }

    private void validateCoordinates(int x0, int y0, int x1, int y1) {
        Preconditions.checkArgument(x0 <= x1 && y0 <= y1,
            "Invalid grid coordinates: " + x0 + ", " + y0 + ", " + x1 + ", " + y1);
    }

    private boolean contains(int x, int y) {
        return x >= gridX0 && x < gridX1 && y >= gridY0 && y < gridY1;
    }

    private void resize(int x0, int y0, int x1, int y1) {
        validateCoordinates(x0, y0, x1, y1);

        // Create a helper grid because it makes coordinate conversion
        // a lot easier.
        Grid<E> helper = new Grid<>(x0, y0, x1, y1);

        for (int x = gridX0; x < gridX1; x++) {
            for (int y = gridY0; y < gridY1; y++) {
                helper.set(x, y, get(x, y));
            }
        }

        // Copy the data from the helper grid back into this instance.
        gridX0 = helper.gridX0;
        gridY0 = helper.gridY0;
        gridX1 = helper.gridX1;
        gridY1 = helper.gridY1;
        cells = helper.cells;
    }

    /**
     * Adds or changed the value of the grid cell located at the specified
     * coordinates. If this is located outside the current grid, the grid
     * will be resized in order to accomodate the new cell.
     */
    public void set(int x, int y, E value) {
        if (!contains(x, y)) {
            resize(
                Math.min(x, gridX0),
                Math.min(y, gridY0),
                Math.max(x + 1, gridX1),
                Math.max(y + 1, gridY1)
            );
        }

        cells[x - gridX0][y - gridY0] = value;
    }

    /**
     * Returns the value of the grid cell located at the specified coordinates.
     * This will return {@code null} if the coordinates are located outside the
     * grid, or if the coordinates are located <em>within</em> the grid but
     * that cell does not have a value.
     */
    public E get(int x, int y) {
        if (!contains(x, y)) {
            return null;
        }

        return cells[x - gridX0][y - gridY0];
    }

    /**
     * Returns a stream containing all elements in this grid. {@code null}
     * elements will <em>not</em> be included in the stream.
     */
    public Stream<E> stream() {
        return Arrays.stream(cells)
            .flatMap(Arrays::stream)
            .filter(value -> value != null);
    }

    /**
     * Returns a stream containing all elements in the specified rectangular
     * sub-grid. The coordinates are <em>exclusive</em>. {@code null} elements
     * will <em>not</em> be included in the stream.
     */
    public Stream<E> stream(int x0, int y0, int x1, int y1) {
        validateCoordinates(x0, y0, x1, y1);

        Stream.Builder<E> stream = Stream.builder();

        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if (cells[x - gridX0][y - gridY0] != null) {
                    stream.add(cells[x - gridX0][y - gridY0]);
                }
            }
        }

        return stream.build();
    }
}
