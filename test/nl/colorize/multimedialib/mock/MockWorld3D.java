//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.mock;

import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.renderer.World3D;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Mesh;

public class MockWorld3D implements World3D {

    @Override
    public Mesh createMesh(Shape3D shape, ColorRGB color) {
        return new MockMesh();
    }

    @Override
    public Point2D project(Point3D position) {
        return Point2D.ORIGIN;
    }

    @Override
    public boolean castPickRay(Point2D canvasPosition, Box area) {
        return false;
    }
}
