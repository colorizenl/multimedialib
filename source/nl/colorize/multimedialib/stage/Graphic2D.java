//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Updatable;

/**
 * Shared interface for all types of 2D graphics. It defines a common API for
 * displaying and positioning graphics, without describing the actual graphics
 * themselves.
 */
public interface Graphic2D extends Updatable {

    public boolean isVisible();

    default void setPosition(Point2D position) {
        getPosition().set(position);
    }

    default void setPosition(float x, float y) {
        getPosition().set(x, y);
    }

    public Point2D getPosition();

    /**
     * Returns a bounding rectangle that describes this graphic's current
     * position and size. If the graphic is not rectangular itself, this
     * will return the smallest axis-aligned rectangle that contains this
     * graphic.
     */
    public Rect getBounds();

    /**
     * Returns true if this graphic contains the specified point. This does
     * not consider different layers or overlapping graphics: as long as
     * the display area contains the point it is considered a hit.
     */
    public boolean hitTest(Point2D point);
}
