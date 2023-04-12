//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Groups multiple graphics that somehow belong together, similar to groups in
 * various 2D animation tools. This allows the graphics to be added to the
 * stage in a single operation, and also allows some bulk operations on all
 * graphics in the group.
 */
public class Group implements Iterable<Graphic2D> {

    private List<Graphic2D> graphics;

    public Group(Graphic2D initial, Graphic2D... rest) {
        graphics = new ArrayList<>();
        graphics.add(initial);
        for (Graphic2D graphic : rest) {
            add(graphic);
        }
    }

    public void add(Graphic2D graphic) {
        graphics.add(graphic);
    }

    public void add(Group group) {
        group.forEach(this::add);
    }

    public void add(Graphic2D graphic, Point2D position) {
        add(graphic);
        graphic.setPosition(position);
    }

    public void add(Graphic2D graphic, float x, float y) {
        add(graphic);
        graphic.setPosition(x, y);
    }

    /**
     * Adds a graphic to this group, then repositions it relative to the first
     * graphic in the group.
     */
    public void addOffset(Graphic2D graphic, float offsetX, float offsetY) {
        add(graphic);

        Point2D position = graphics.get(0).getPosition();
        graphic.setPosition(position.getX() + offsetX, position.getY() + offsetY);
    }

    @Override
    public Iterator<Graphic2D> iterator() {
        return graphics.iterator();
    }

    public Stream<Graphic2D> stream() {
        return graphics.stream();
    }

    public <T extends Graphic2D> void forEach(Class<T> type, Consumer<T> callback) {
        graphics.stream()
            .filter(graphic -> graphic.getClass().equals(type))
            .forEach(graphic -> callback.accept((T) graphic));
    }

    /**
     * Returns the bounding rectangle for this group, which is the smallest
     * rectangle that can contain all graphics within the group.
     */
    public Rect getBounds() {
        Rect start = graphics.get(0).getBounds();
        float x0 = start.getX();
        float y0 = start.getY();
        float x1 = start.getEndX();
        float y1 = start.getEndY();

        for (int i = 1; i < graphics.size(); i++) {
            Rect gb = graphics.get(i).getBounds();
            x0 = Math.min(x0, gb.getX());
            y0 = Math.min(y0, gb.getY());
            x1 = Math.max(x1, gb.getEndX());
            y1 = Math.max(y1, gb.getEndY());
        }

        return Rect.fromPoints(x0, y0, x1, y1);
    }

    public void setVisible(boolean visible) {
        for (Graphic2D graphic : graphics) {
            graphic.setVisible(visible);
        }
    }
}
