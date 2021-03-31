//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.PolygonModel;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;

/**
 * Interface for programmatically create 3D polygon models. This generally
 * concerns simple primitives. More complex models can be loaded from files
 * using {@link MediaLoader}.
 */
public interface GeometryBuilder {

    public PolygonModel createQuad(Point2D size, ColorRGB color);

    public PolygonModel createQuad(Point2D size, Image texture);

    public PolygonModel createBox(Point3D size, ColorRGB color);

    public PolygonModel createBox(Point3D size, Image texture);

    public PolygonModel createSphere(float diameter, ColorRGB color);

    public PolygonModel createSphere(float diameter, Image texture);
}
