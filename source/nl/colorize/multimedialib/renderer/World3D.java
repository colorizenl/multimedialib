//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Box;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Point3D;
import nl.colorize.multimedialib.math.Shape3D;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Mesh;

/**
 * Lets the stage access the underlying 3D world. This is only supported when
 * using a renderer that supports 3D graphics.
 */
public interface World3D {

    /**
     * Programmatically creates a 3D polygon mesh with a solid color, based
     * on the specified shape.
     *
     * @throws UnsupportedOperationException if this renderer does not
     *         support 3D graphics.
     */
    public Mesh createMesh(Shape3D shape, ColorRGB color);

    /**
     * Programmatically creates a 3D polygon mesh that initially does not
     * have any color or texture information attached to it. The mesh can
     * be modified after creation using {@link Mesh#applyColor(ColorRGB)}
     * and {@link Mesh#applyTexture(Image)} respectively.
     *
     * @throws UnsupportedOperationException if this renderer does not
     *         support 3D graphics.
     */
    default Mesh createMesh(Shape3D shape) {
        return createMesh(shape, ColorRGB.WHITE);
    }

    /**
     * Returns the 3D world coordinates that correspond to the specified 2D
     * canvas coordinates, based on the current camera position.
     *
     * @throws UnsupportedOperationException if this renderer does not
     *         support 3D graphics.
     */
    public Point2D project(Point3D position);

    /**
     * Casts a pick ray from the specified 2D canvas position and returns true
     * if the pick ray intersects with the specified 3D world coordinates.
     *
     * @throws UnsupportedOperationException if this renderer does not
     *         support 3D graphics.
     */
    public boolean castPickRay(Point2D canvasPosition, Box area);
}
