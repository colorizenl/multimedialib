//-----------------------------------------------------------------------------
// Ape Attack
// Copyright 2005, 2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Point2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Groups multiple graphics that somehow belong together, similar to groups in
 * various 2D animation tools. This allows the graphics to be added to the
 * stage in a single operation, and this class also allows some bulk operations
 * on all graphics in the group.
 */
public class Group implements Iterable<Graphic2D> {

    private List<Graphic2D> graphics;

    public Group(Graphic2D... initialGraphics) {
        this.graphics = new ArrayList<>();
        for (Graphic2D graphic : initialGraphics) {
            add(graphic);
        }
    }

    public void add(Graphic2D graphic) {
        graphics.add(graphic);
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
        Preconditions.checkState(!graphics.isEmpty(), "Empty group");
        add(graphic);

        Point2D position = graphics.get(0).getPosition();
        graphic.setPosition(position.getX() + offsetX, position.getY() + offsetY);
    }

    @Override
    public Iterator<Graphic2D> iterator() {
        return graphics.iterator();
    }

    public Stream<Graphic2D> match(Predicate<Graphic2D> filter) {
        return graphics.stream().filter(filter);
    }

    public boolean hitTest(Point2D point) {
        return graphics.stream()
            .anyMatch(graphic -> graphic.hitTest(point));
    }
}
