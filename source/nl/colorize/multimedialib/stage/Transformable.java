//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Angle;
import nl.colorize.multimedialib.math.Point2D;

/**
 * Read-only interface for reading {@link Transform} properties. All exposed
 * properties have the same definitions as those in {@link Transform}.
 */
public interface Transformable {

    public boolean isVisible();

    public Point2D getPosition();

    default float getX() {
        return getPosition().x();
    }

    default float getY() {
        return getPosition().y();
    }

    public Angle getRotation();

    public float getScaleX();

    public float getScaleY();

    public boolean isFlipHorizontal();

    public boolean isFlipVertical();

    public float getAlpha();

    public ColorRGB getMaskColor();
}
